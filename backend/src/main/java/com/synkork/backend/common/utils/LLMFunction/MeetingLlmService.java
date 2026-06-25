package com.synkork.backend.common.utils.LLMFunction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
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

    private static final String[] SUPPORTED_AUDIO_FORMATS = {"mp3", "m4a", "webm"};
    private static final String   DEFAULT_AUDIO_FORMAT    = "wav";

    private final OpenRouterClient openRouterClient;

    public MeetingLlmService(OpenRouterClient openRouterClient) {
        this.openRouterClient = openRouterClient;
    }

    // ── Public API ────────────────────────────────────────────────────────────

    public String transcribeAudio(MultipartFile audioFile) {
        if (!openRouterClient.isConfigured()) return "[API Key missing]";
        try {
            byte[] bytes = audioFile.getBytes();
            return openRouterClient.chatCompletion(
                    LlmPrompts.REFERER_DEFAULT,
                    LlmPrompts.APP_TITLE,
                    LlmPrompts.MODEL_TRANSCRIPTION,
                    List.of(buildTranscriptionMessage(audioFile, bytes)),
                    false
            );
        } catch (Exception e) {
            log.error("Lỗi transcribe audio", e);
            return "";
        }
    }

    public String summarizeMeeting(String transcript) {
        if (!openRouterClient.isConfigured()) return "{}";
        try {
            String raw = openRouterClient.chatCompletion(
                    LlmPrompts.REFERER_DEFAULT,
                    LlmPrompts.APP_TITLE,
                    LlmPrompts.MODEL_MEETING_SUMMARY,
                    List.of(Map.of("role", "user", "content",
                            LlmPrompts.MEETING_SUMMARY_PROMPT_TEMPLATE.formatted(transcript))),
                    true
            );
            return openRouterClient.parseJsonOrFallback(raw, "{}");
        } catch (Exception e) {
            log.error("Lỗi summarize meeting", e);
            return "{}";
        }
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
