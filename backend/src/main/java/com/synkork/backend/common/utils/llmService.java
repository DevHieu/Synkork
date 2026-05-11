package com.synkork.backend.common.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class llmService {
    @Value("${OPENROUTER_API_KEY:}")
    private String openRouterApiKey;

    @Value("${OPENROUTER_BASE_URL:https://openrouter.ai/api/v1}")
    private String openRouterBaseUrl;

    public String detectEventFromMessage(String messageContent) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openRouterApiKey);
         headers.set("HTTP-Referer", "http://localhost:5173/rooms/chat");
         headers.set("X-Title", "Synkork");

        String prompt = """
You are an AI assistant.
Current time: %s.
Task: Analyze the input message and return the result in JSON format with the structure:
{ 'hasEvent': boolean, 'title': string, 'eventDate': string, 'startTime': string }.
Note:
Title would be, 
ex : message: mai 9h nhé => mai 9h 
Must be delete "nhé"
ex : message: mai 9h nhé có tôi và Hiếu => mai 9h có Username và Hiếu
Must be delete "nhé" or anything does't relate to event 
Rules:
- If the message is NOT related to event creation → return null.
- If an event exists → extract all fields accurately.
- All returned content (including 'title') MUST be in Vietnamese.
- Do not include anything outside the JSON.

Message: "%s"
""".formatted(LocalDateTime.now(), messageContent);

        System.out.println(prompt);
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "nvidia/nemotron-3-super-120b-a12b:free");
        Map<String, String> userMessage = new HashMap<>();
        userMessage.put("role", "user");
        userMessage.put("content", prompt);

        requestBody.put("messages", List.of(userMessage));
        requestBody.put("response_format", Map.of("type", "json_object"));

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                openRouterBaseUrl + "/chat/completions",
                HttpMethod.POST,
                entity,
                String.class
        );

        System.out.println(response.getBody());

        // Kết quả sẽ chứa JSON do LLM sinh ra
        return response.getBody();
    }
}
