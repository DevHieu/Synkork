package com.synkork.backend.common.utils.LLMFunction;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Lớp transport dùng chung cho mọi cuộc gọi LLM qua OpenRouter; không chứa luật nghiệp vụ.
 */
@Service
public class OpenRouterClient {

    private static final Logger log = LoggerFactory.getLogger(OpenRouterClient.class);

    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;

    @Value("${OPENROUTER_API_KEY:}")
    private String openRouterApiKey;

    @Value("${OPENROUTER_BASE_URL:https://openrouter.ai/api/v1}")
    private String openRouterBaseUrl;

    public OpenRouterClient(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.restTemplate = createRestTemplate();
    }

    public boolean isConfigured() {
        return openRouterApiKey != null && !openRouterApiKey.isBlank();
    }

    /**
     * Gọi API chat completion của OpenRouter.
     *
     * @param referer      HTTP-Referer header
     * @param title        X-Title header
     * @param model        ID mô hình OpenRouter
     * @param messages     Danh sách message theo format OpenAI
     * @param jsonResponse Yêu cầu response trả về JSON object
     * @return Nội dung text từ model
     */
    public String chatCompletion(
            String referer,
            String title,
            String model,
            List<Map<String, Object>> messages,
            boolean jsonResponse
    ) throws Exception {
        if (!isConfigured()) {
            throw new IllegalStateException("OPENROUTER_API_KEY is missing");
        }

        Map<String, Object> requestBody = new HashMap<>(4);
        requestBody.put("model", model);
        requestBody.put("messages", messages);
        if (jsonResponse) {
            requestBody.put("response_format", Map.of("type", "json_object"));
        }

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, buildHeaders(referer, title));
        ResponseEntity<String> response = restTemplate.exchange(
                openRouterBaseUrl + "/chat/completions",
                HttpMethod.POST,
                entity,
                String.class
        );

        String responseBody = response.getBody();
        logUsage(model, responseBody);
        return extractContent(responseBody);
    }

    /**
     * Kiểm tra rawJson có phải JSON hợp lệ không; nếu không trả về fallback.
     */
    public String parseJsonOrFallback(String raw, String fallback) {
        if (raw == null || raw.isBlank()) return fallback;
        try {
            objectMapper.readTree(raw.trim());
            return raw.trim();
        } catch (Exception e) {
            return fallback;
        }
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private RestTemplate createRestTemplate() {
        RestTemplate template = new RestTemplate();
        template.getMessageConverters().add(0, new StringHttpMessageConverter(StandardCharsets.UTF_8));
        return template;
    }

    private HttpHeaders buildHeaders(String referer, String title) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openRouterApiKey);
        headers.set("HTTP-Referer", referer);
        headers.set("X-Title", title);
        return headers;
    }

    private String extractContent(String responseBody) throws Exception {
        if (responseBody == null || responseBody.isBlank()) return "";
        return objectMapper.readTree(responseBody)
                .path("choices").path(0).path("message").path("content")
                .asText("");
    }

    private void logUsage(String model, String responseBody) {
        if (responseBody == null || responseBody.isBlank()) {
            log.warn("[OpenRouter] model={} usage=empty-response", model);
            return;
        }
        try {
            JsonNode usage = objectMapper.readTree(responseBody).path("usage");
            if (usage.isMissingNode() || usage.isNull()) {
                log.warn("[OpenRouter] model={} usage=missing", model);
                return;
            }
            log.info("[OpenRouter] model={} prompt_tokens={} completion_tokens={} total_tokens={}",
                    model,
                    usage.path("prompt_tokens").asInt(-1),
                    usage.path("completion_tokens").asInt(-1),
                    usage.path("total_tokens").asInt(-1));
        } catch (Exception e) {
            log.warn("[OpenRouter] model={} usage=parse-failed", model);
        }
    }
}
