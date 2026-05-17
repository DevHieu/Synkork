package com.synkork.backend.common.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class llmMeetingService {

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${OPENROUTER_API_KEY:}")
    private String openRouterApiKey;

    @Value("${OPENROUTER_BASE_URL:https://openrouter.ai/api/v1}")
    private String openRouterBaseUrl;

    public String transcribeAudio(MultipartFile audioFile) {
        if (openRouterApiKey == null || openRouterApiKey.isEmpty()) {
            return "[API Key missing]";
        }

        try {
            byte[] fileContent = audioFile.getBytes();
            String base64Audio = Base64.getEncoder().encodeToString(fileContent);
            String fileName = audioFile.getOriginalFilename();
            String format = "wav";
            
            if (fileName != null && fileName.contains(".")) {
                format = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
            }
            
            // Normalize format
            if (format.equals("mp3")) format = "mp3";
            else if (format.equals("m4a")) format = "m4a";
            else if (format.equals("webm")) format = "webm";
            else format = "wav";

            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(openRouterApiKey);
            headers.set("HTTP-Referer", "http://localhost:5173");
            headers.set("X-Title", "Synkork");

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", "nvidia/nemotron-3-nano-omni-30b-a3b-reasoning:free");

            Map<String, Object> audioContent = new HashMap<>();
            audioContent.put("type", "input_audio");
            Map<String, String> inputAudio = new HashMap<>();
            inputAudio.put("data", base64Audio);
            inputAudio.put("format", format);
            audioContent.put("input_audio", inputAudio);

            Map<String, Object> textContent = new HashMap<>();
            textContent.put("type", "text");
            textContent.put("text", "Hãy chuyển âm thanh này thành văn bản tiếng Việt chính xác nhất. Chỉ trả về nội dung văn bản.");

            Map<String, Object> message = new HashMap<>();
            message.put("role", "user");
            message.put("content", List.of(textContent, audioContent));

            requestBody.put("messages", List.of(message));

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            ResponseEntity<String> response = restTemplate.exchange(
                    openRouterBaseUrl + "/chat/completions",
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            JsonNode rootNode = objectMapper.readTree(response.getBody());
            return rootNode.path("choices").get(0).path("message").path("content").asText();
        } catch (Exception e) {
            System.err.println("STT Error: " + e.getMessage());
            return "";
        }
    }

    public String summarizeMeeting(String transcript) {
        System.out.println("--- LLM Summarize Meeting ---");
        System.out.println("Transcript length: " + transcript.length());

        if (openRouterApiKey == null || openRouterApiKey.isEmpty()) {
            return "{}";
        }

        try {
            RestTemplate restTemplate = new RestTemplate();
            restTemplate.getMessageConverters()
                    .add(0, new StringHttpMessageConverter(StandardCharsets.UTF_8));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8));
            headers.setBearerAuth(openRouterApiKey);
            headers.set("HTTP-Referer", "http://localhost:5173");
            headers.set("X-Title", "Synkork");

            String prompt = """
            Tóm tắt nội dung cuộc họp sau đây sang định dạng JSON tiếng Việt:
            {
              "summary": "Tóm tắt ngắn gọn nội dung chính",
              "keyPoints": ["Điểm chính quan trọng 1", "Điểm chính quan trọng 2"],
              "actionItems": ["Việc cần làm sau cuộc họp 1", "Việc cần làm sau cuộc họp 2"]
            }
            Quy tắc:
            1. Trả về JSON hợp lệ.
            2. Sử dụng ngôn ngữ tiếng Việt tự nhiên, chuyên nghiệp.
            
            Nội dung cuộc họp:
            "%s"
            """.formatted(transcript);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", "openai/gpt-oss-120b:free");
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

            JsonNode rootNode = objectMapper.readTree(response.getBody());
            String result = rootNode.path("choices").get(0).path("message").path("content").asText();
            System.out.println("LLM Summary Result: " + result);
            return result;
        } catch (Exception e) {
            System.err.println("Summary Error: " + e.getMessage());
            return "{}";
        }
    }
}
