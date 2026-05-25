package com.synkork.backend.common.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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

@Service
public class OpenRouterClient {
    // Lớp transport dùng chung cho mọi cuộc gọi LLM; không chứa luật nghiệp vụ.

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

        // Dựng body request một lần rồi gửi qua client dùng chung.
        Map<String, Object> requestBody = new HashMap<>(4);
        requestBody.put("model", model);
        requestBody.put("messages", messages);
        if (jsonResponse) {
            requestBody.put("response_format", Map.of("type", "json_object"));
        }

        HttpHeaders headers = buildHeaders(referer, title);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

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
        if (responseBody == null || responseBody.isBlank()) {
            return "";
        }

        JsonNode rootNode = objectMapper.readTree(responseBody);
        return rootNode.path("choices")
                .path(0)
                .path("message")
                .path("content")
                .asText("");
    }

    private void logUsage(String model, String responseBody) {
        if (responseBody == null || responseBody.isBlank()) {
            System.out.println("[OpenRouter] model=" + model + " usage=empty-response");
            return;
        }

        try {
            JsonNode rootNode = objectMapper.readTree(responseBody);
            JsonNode usageNode = rootNode.path("usage");

            if (usageNode.isMissingNode() || usageNode.isNull()) {
                System.out.println("[OpenRouter] model=" + model + " usage=missing");
                return;
            }

            int promptTokens = usageNode.path("prompt_tokens").asInt(-1);
            int completionTokens = usageNode.path("completion_tokens").asInt(-1);
            int totalTokens = usageNode.path("total_tokens").asInt(-1);
            System.out.println("[OpenRouter] model=" + model
                    + " prompt_tokens=" + promptTokens
                    + " completion_tokens=" + completionTokens
                    + " total_tokens=" + totalTokens);
        } catch (Exception e) {
            System.out.println("[OpenRouter] model=" + model + " usage=parse-failed");
        }
    }
}
