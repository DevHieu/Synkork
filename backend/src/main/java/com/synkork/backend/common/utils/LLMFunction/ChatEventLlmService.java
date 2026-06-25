package com.synkork.backend.common.utils.LLMFunction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Phát hiện ý định tạo event/task/note từ tin nhắn chat; gọi LLM qua OpenRouter.
 * Prompt và model ID được quản lý tập trung tại {@link LlmPrompts}.
 */
@Service
public class ChatEventLlmService {

    private static final Logger log = LoggerFactory.getLogger(ChatEventLlmService.class);

    private static final ZoneId            BANGKOK_ZONE = ZoneId.of("Asia/Bangkok");
    private static final DateTimeFormatter DATE_FMT     = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATETIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private static final int     MIN_LENGTH              = 10;
    private static final int     MAX_CONSECUTIVE_REPEATS = 3;

    /** Danh sách model dự phòng, thử theo thứ tự từ trên xuống dưới. */
    private static final List<String> CHAT_EVENT_MODELS = List.of(
            "openai/gpt-oss-120b:free",
            "poolside/laguna-m.1:free",
            "nvidia/nemotron-3-super-120b-a12b:free",
            "z-ai/glm-4.5-air:free"
    );

    /** Tin nhắn chỉ gồm chữ số (vd: "12345"). */
    private static final Pattern ONLY_DIGITS   = Pattern.compile("^\\d+$");

    /** Tin nhắn chỉ gồm ký tự đặc biệt / dấu câu (vd: "!!!??.."). */
    private static final Pattern ONLY_SYMBOLS  = Pattern.compile("^[^\\p{L}\\p{N}]+$");

    /** Keyboard mashing: ≥5 ký tự Latin liên tiếp không có nguyên âm (vd: "sdflkj", "qwrtyp"). */
    private static final Pattern KEYBOARD_MASH = Pattern.compile("[bcdfghjklmnpqrstvwxyz]{5,}", Pattern.CASE_INSENSITIVE);

    private final OpenRouterClient openRouterClient;

    public ChatEventLlmService(OpenRouterClient openRouterClient) {
        this.openRouterClient = openRouterClient;
    }

    // ── Public API ────────────────────────────────────────────────────────────

    public String detectSuggestionFromMessage(String messageContent) {
        if (!openRouterClient.isConfigured()) return "{}";
        if (!isWorthAnalyzing(messageContent)) {
            log.debug("Đã chặn tạo nhanh cho tin nhắn '{}' vì không phù hợp format", messageContent);
            return "{}";
        }
        try {
            ZonedDateTime now = ZonedDateTime.now(BANGKOK_ZONE);
            String raw = callWithFallback(now, messageContent);
            return openRouterClient.parseJsonOrFallback(raw, "{}");
        } catch (Exception e) {
            log.error("Lỗi khi gọi OpenRouter / LLM", e);
            return "{}";
        }
    }

    private String callWithFallback(ZonedDateTime now, String messageContent) throws Exception {
        Exception lastException = null;
        List<Map<String, Object>> messages = buildMessages(now, messageContent);

        for (String model : CHAT_EVENT_MODELS) {
            try {
                return openRouterClient.chatCompletion(
                        LlmPrompts.REFERER_CHAT,
                        LlmPrompts.APP_TITLE,
                        model,
                        messages,
                        true
                );
            } catch (RestClientException e) {
                lastException = e;
                log.warn("Model {} thất bại, thử model dự phòng tiếp theo: {}",
                         model, e.getMessage());
            }
        }

        if (lastException != null) {
            throw lastException;
        }

        throw new IllegalStateException("No fallback model available");
    }

    // ── Pre-filter ────────────────────────────────────────────────────────────

    /**
     * Trả về {@code false} nếu tin nhắn không đáng gửi lên
     */
    boolean isWorthAnalyzing(String message) {
        if (message == null) return false;

        String trimmed = message.trim();

        if (trimmed.length() < MIN_LENGTH)               return false;
        if (hasExcessiveRepeats(trimmed))                return false;
        if (ONLY_DIGITS.matcher(trimmed).matches())      return false;
        if (ONLY_SYMBOLS.matcher(trimmed).matches())     return false;
        if (KEYBOARD_MASH.matcher(trimmed).find())       return false;

        return true;
    }

    /**
     * Kiểm tra chuỗi có ký tự nào lặp liên tiếp
     */
    private boolean hasExcessiveRepeats(String s) {
        int count = 1;
        for (int i = 1; i < s.length(); i++) {
            if (s.charAt(i) == s.charAt(i - 1)) {
                if (++count > MAX_CONSECUTIVE_REPEATS) return true;
            } else {
                count = 1;
            }
        }
        return false;
    }

    private record DateRef(String today, String tomorrow, String dayAfterTomorrow) {
        static DateRef of(ZonedDateTime now) {
            return new DateRef(
                    now.format(DATE_FMT),
                    now.plusDays(1).format(DATE_FMT),
                    now.plusDays(2).format(DATE_FMT)
            );
        }
    }

    private List<Map<String, Object>> buildMessages(ZonedDateTime now, String messageContent) {
        DateRef d = DateRef.of(now);
        return List.of(
                Map.of("role", "system", "content", LlmPrompts.CHAT_EVENT_SYSTEM_PROMPT),
                Map.of("role", "user", "content",
                        LlmPrompts.CHAT_EVENT_USER_PROMPT_TEMPLATE.formatted(
                                now.format(DATETIME_FMT),
                                d.today(), d.tomorrow(), d.dayAfterTomorrow(),
                                messageContent))
        );
    }
}
