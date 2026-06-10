package com.synkork.backend.common.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Base64;
import java.util.List;
import java.util.Map;

@Service
public class MeetingLlmService {
    // Luồng voice cuộc họp tách riêng với phát hiện event chat vì kiểu đầu vào khác nhau.
    private static final String REFERER = "http://localhost:5173";
    private static final String APP_TITLE = "Synkork";
    private static final String TRANSCRIPTION_MODEL = "nvidia/nemotron-3-nano-omni-30b-a3b-reasoning:free";
    private static final String SUMMARY_MODEL = "moonshotai/kimi-k2.6:free";

    private final ObjectMapper objectMapper;
    private final OpenRouterClient openRouterClient;

    public MeetingLlmService(ObjectMapper objectMapper, OpenRouterClient openRouterClient) {
        this.objectMapper = objectMapper;
        this.openRouterClient = openRouterClient;
    }

    private static final String SUMMARY_PROMPT_TEMPLATE = """
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
            """;

    public String transcribeAudio(MultipartFile audioFile) {
        // 1) Trả về sớm nếu OpenRouter chưa được cấu hình.
        if (!openRouterClient.isConfigured()) {
            return "[API Key missing]";
        }

        try {
            // 2) Mã hóa audio và gửi request STT đa phương thức.
            byte[] fileContent = audioFile.getBytes();
            Map<String, Object> message = buildTranscriptionMessage(audioFile, fileContent);
            return openRouterClient.chatCompletion(
                    REFERER,
                    APP_TITLE,
                    TRANSCRIPTION_MODEL,
                    List.of(message),
                    false
            );
        } catch (Exception e) {
            return "";
        }
    }

    public String summarizeMeeting(String transcript) {
        // 1) Trả về sớm nếu OpenRouter chưa được cấu hình.
        if (!openRouterClient.isConfigured()) {
            return "{}";
        }

        try {
            // 2) Yêu cầu model tóm tắt trả về JSON có cấu trúc.
            String prompt = SUMMARY_PROMPT_TEMPLATE.formatted(transcript);
            String rawResult = openRouterClient.chatCompletion(
                    REFERER,
                    APP_TITLE,
                    SUMMARY_MODEL,
                    List.of(Map.of("role", "user", "content", prompt)),
                    true
            );
            // 3) Giữ kiểm tra cục bộ và đơn giản: chỉ nhận JSON hợp lệ.
            return parseJsonOrFallback(rawResult);
        } catch (Exception e) {
            return "{}";
        }
    }

    private Map<String, Object> buildTranscriptionMessage(MultipartFile audioFile, byte[] fileContent) {
        // OpenRouter yêu cầu một user message duy nhất gồm phần text và phần audio.
        return Map.of(
                "role", "user",
                "content", List.of(
                        buildTextContent(),
                        buildAudioContent(audioFile, fileContent)
                )
        );
    }

    private Map<String, Object> buildTextContent() {
        return Map.of(
                "type", "text",
                "text", "Hãy chuyển âm thanh này thành văn bản tiếng Việt chính xác nhất. Chỉ trả về nội dung văn bản."
        );
    }

    private Map<String, Object> buildAudioContent(MultipartFile audioFile, byte[] fileContent) {
        return Map.of(
                "type", "input_audio",
                "input_audio", Map.of(
                        "data", Base64.getEncoder().encodeToString(fileContent),
                        "format", resolveAudioFormat(audioFile.getOriginalFilename())
                )
        );
    }

    private String resolveAudioFormat(String fileName) {
        String format = "wav";

        if (fileName != null && fileName.contains(".")) {
            format = fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
        }

        if (!"mp3".equals(format) && !"m4a".equals(format) && !"webm".equals(format)) {
            return "wav";
        }

        return format;
    }

    private String parseJsonOrFallback(String rawResult) {
        if (rawResult == null || rawResult.isBlank()) {
            return "{}";
        }

        try {
            objectMapper.readTree(rawResult.trim());
            return rawResult.trim();
        } catch (Exception e) {
            return "{}";
        }
    }
}
