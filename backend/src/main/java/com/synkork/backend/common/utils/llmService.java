package com.synkork.backend.common.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class llmService {
    @Autowired
    private ObjectMapper objectMapper;

    @Value("${OPENROUTER_API_KEY:}")
    private String openRouterApiKey;

    @Value("${OPENROUTER_BASE_URL:https://openrouter.ai/api/v1}")
    private String openRouterBaseUrl;

    public String detectEventFromMessage(String messageContent) {
        System.out.println("--- LLM Detect Event ---");
        System.out.println("Message: " + messageContent);

        // Tự động nhận diện sự kiện từ nội dung tin nhắn chat
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getMessageConverters()
                .add(0, new StringHttpMessageConverter(StandardCharsets.UTF_8));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8));
        headers.setBearerAuth(openRouterApiKey);
        headers.set("HTTP-Referer", "http://localhost:5173/rooms/chat");
        headers.set("X-Title", "Synkork");

        String prompt = """
Extract Vietnamese event info to JSON: {"hasEvent": bool, "title": string, "eventDate": "YYYY-MM-DD", "startTime": "HH:mm"}
Current time: %s
Rules:
1. Replace "tôi/mình" with "Username", remove filler ("nhé", "nha", "đi").
2. Calculate relative dates ("mai", "mốt", "nay") based on Current time.
3. If no specific time found, startTime: "".
4. If NOT an event creation, set hasEvent: false and other fields to null.

Few-shot:
- "mai 9h nhé có tôi và Hiếu" -> {"hasEvent": true, "title": "mai 9h có Username và Hiếu", "eventDate": "2026-05-12", "startTime": "09:00"}
- "họp lúc 3h chiều nay" -> {"hasEvent": true, "title": "họp lúc 3h chiều nay", "eventDate": "2026-05-11", "startTime": "15:00"}
- "hello mọi người" -> {"hasEvent": false, "title": null, "eventDate": null, "startTime": null}

Message: "%s"
""".formatted(LocalDateTime.now(), messageContent);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "openai/gpt-oss-120b:free");
        Map<String, String> userMessage = new HashMap<>();
        userMessage.put("role", "user");
        userMessage.put("content", prompt);

        requestBody.put("messages", List.of(userMessage));
        requestBody.put("response_format", Map.of("type", "json_object"));

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    openRouterBaseUrl + "/chat/completions",
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            JsonNode rootNode = objectMapper.readTree(response.getBody());
            String rawResult = rootNode.path("choices").get(0).path("message").path("content").asText();
            String sanitizedResult = sanitizeJsonResult(rawResult);
            System.out.println("LLM Result: " + sanitizedResult);
            return sanitizedResult;
        } catch (Exception e) {
            System.err.println("Lỗi phân tích tin nhắn: " + e.getMessage());
            return "{}";
        }
    }

    private String sanitizeJsonResult(String rawResult) {
        String sanitized = LlmJsonSanitizer.sanitize(rawResult);

        try {
            objectMapper.readTree(sanitized);
            return sanitized;
        } catch (Exception validationError) {
            System.err.println("LLM detectEvent trả về JSON không hợp lệ sau khi sanitize: " + sanitized);
            return "{}";
        }
    }
}
