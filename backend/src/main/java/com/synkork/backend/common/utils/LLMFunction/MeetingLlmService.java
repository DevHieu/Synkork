package com.synkork.backend.common.utils.LLMFunction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.multipart.MultipartFile;

import java.util.Base64;
import java.util.List;
import java.util.Map;

/**
 * Xử lý luồng voice cuộc họp: chuyển âm thanh thành văn bản và tóm tắt nội dung họp.
 * Tách riêng với {@link ChatEventLlmService} vì kiểu đầu vào (audio vs text) khác nhau.
 * Prompt và model ID được quản lý tập trung tại {@link LlmPrompts}.
 */
@Service
public class MeetingLlmService {

    private static final Logger log = LoggerFactory.getLogger(MeetingLlmService.class);



    private final OpenRouterClient openRouterClient;

    public MeetingLlmService(OpenRouterClient openRouterClient) {
        this.openRouterClient = openRouterClient;
    }

    /** Cho phép controller kiểm tra sớm trước khi gọi LLM. */
    public boolean isConfigured() {
        return openRouterClient.isConfigured();
    }

    // ── Public API ────────────────────────────────────────────────────────────

    public String transcribeAudio(MultipartFile audioFile) throws Exception {
        if (!openRouterClient.isConfigured()) {
            throw new IllegalStateException("OPENROUTER_API_KEY chưa được cấu hình.");
        }

        byte[] bytes = audioFile.getBytes();
        String base64Audio = Base64.getEncoder().encodeToString(bytes);

        // Mặc định ép sang 'mp3' để lừa JSON Schema Validation của OpenRouter 
        // (OpenRouter chỉ cho phép 'wav' hoặc 'mp3' theo format của OpenAI).
        // Model bên dưới (Gemini) thường tự nhận diện header file nên vẫn giải mã được WebM.
        String format = "mp3";

        List<Map<String, Object>> contentArray = List.of(
                Map.of("type", "text", "text", LlmPrompts.MEETING_TRANSCRIPTION_INSTRUCTION),
                Map.of("type", "input_audio", "input_audio", Map.of(
                        "data", base64Audio,
                        "format", format
                ))
        );

        List<Map<String, Object>> messages = List.of(
                Map.of("role", "user", "content", contentArray)
        );

        log.info("[MeetingLlmService] Đang gọi OpenRouter để bóc băng ghi âm (model: {})", LlmPrompts.MODEL_TRANSCRIPTION);
        
        return openRouterClient.chatCompletion(
                LlmPrompts.REFERER_DEFAULT,
                LlmPrompts.APP_TITLE,
                LlmPrompts.MODEL_TRANSCRIPTION,
                messages,
                false // không bắt buộc JSON
        );
    }

    public String summarizeMeeting(String transcript) {
        if (!openRouterClient.isConfigured()) return "{}";
        
        Exception lastException = null;
        List<Map<String, Object>> messages = List.of(Map.of("role", "user", "content",
                LlmPrompts.MEETING_SUMMARY_PROMPT_TEMPLATE.formatted(transcript)));

        for (String model : LlmPrompts.MEETING_SUMMARY_MODELS) {
            try {
                String raw = openRouterClient.chatCompletion(
                        LlmPrompts.REFERER_DEFAULT,
                        LlmPrompts.APP_TITLE,
                        model,
                        messages,
                        true
                );
                return openRouterClient.parseJsonOrFallback(raw, "{}");
            } catch (RestClientException e) {
                lastException = e;
                log.warn("Model {} thất bại, thử model dự phòng tiếp theo: {}",
                         model, e.getMessage());
            } catch (Exception e) {
                lastException = e;
                log.warn("Lỗi không mong muốn với model {}: {}", model, e.getMessage());
            }
        }
        
        log.error("Tất cả các model tóm tắt cuộc họp đều thất bại", lastException);
        return "{}";
    }

}
