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

    private static final String[] SUPPORTED_AUDIO_FORMATS = {"mp3", "m4a", "webm", "ogg", "wav"};
    private static final String   DEFAULT_AUDIO_FORMAT    = "wav";

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
        byte[] bytes = audioFile.getBytes();
        return openRouterClient.chatCompletion(
                LlmPrompts.REFERER_DEFAULT,
                LlmPrompts.APP_TITLE,
                LlmPrompts.MODEL_TRANSCRIPTION,
                List.of(buildTranscriptionMessage(audioFile, bytes)),
                false
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

    // ── Helpers ───────────────────────────────────────────────────────────────

    /**
     * OpenRouter yêu cầu một user message duy nhất gồm phần text và phần audio.
     */
    private Map<String, Object> buildTranscriptionMessage(MultipartFile audioFile, byte[] bytes) {
        return Map.of(
                "role", "user",
                "content", List.of(
                        Map.of("type", "text",
                               "text", LlmPrompts.MEETING_TRANSCRIPTION_INSTRUCTION),
                        Map.of("type", "input_audio",
                               "input_audio", Map.of(
                                       "data", Base64.getEncoder().encodeToString(bytes),
                                       "format", resolveAudioFormat(audioFile.getOriginalFilename())))
                )
        );
    }

    private String resolveAudioFormat(String fileName) {
        if (fileName != null && fileName.contains(".")) {
            String ext = fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
            for (String supported : SUPPORTED_AUDIO_FORMATS) {
                if (supported.equals(ext)) return ext;
            }
        }
        return DEFAULT_AUDIO_FORMAT;
    }
}
