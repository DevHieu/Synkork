package com.synkork.backend.common.utils.LLMFunction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
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

    /** Tin nhắn chỉ gồm chữ số (vd: "12345"). */
    private static final Pattern ONLY_DIGITS   = Pattern.compile("^\\d+$");

    /** Tin nhắn chỉ gồm ký tự đặc biệt / dấu câu (vd: "!!!??.."). */
    private static final Pattern ONLY_SYMBOLS  = Pattern.compile("^[^\\p{L}\\p{N}]+$");

    /** Keyboard mashing: ≥5 ký tự Latin liên tiếp không có nguyên âm (vd: "sdflkj", "qwrtyp"). */
    private static final Pattern KEYBOARD_MASH = Pattern.compile("[bcdfghjklmnpqrstvwxyz]{5,}", Pattern.CASE_INSENSITIVE);

    private static final String[] EVENT_KEYWORDS = {
        // Tiếng Việt
        "sự kiện", "lịch", "lịch hẹn", "hẹn", "họp", "cuộc họp", "meeting",
        "hội nghị", "hội thảo", "tiệc", "party", "concert", "sinh nhật",
        "đám cưới", "offline", "online", "seminar", "workshop",
        "mai", "mốt", "hôm nay", "ngày mai",
        "thứ", "tuần", "tháng", "năm",
        "cuối tuần", "đầu tuần","tuần sau","tuần tới",
        "lúc", "vào", "ngày", "giờ", "chiều", "sáng", "tối","sau","tới",

        // English
        "event", "appointment", "schedule", "calendar",
        "conference", "webinar", "call", "zoom", "meet"
    };

    private static final String[] NOTE_KEYWORDS = {
        // Tiếng Việt
        "ghi chú", "ghi lại", "lưu", "lưu lại", "note",
        "nhớ", "ghi nhớ", "thông tin", "ý tưởng",
        "idea", "memo", "nhật ký", "log",

        // English
        "note", "memo", "remember", "save", "write down",
        "information", "idea"
    };

    private static final String[] TASK_KEYWORDS = {
        // Tiếng Việt
        "việc", "công việc", "nhiệm vụ", "task", "todo",
        "to do", "cần", "phải", "làm", "hoàn thành",
        "deadline", "hạn", "hạn chót", "nộp",
        "nhắc", "nhắc nhở", "reminder",

        // English
        "task", "todo", "to-do", "remind", "reminder",
        "finish", "complete", "submit", "due",
        "deadline", "assignment"
    };

    private static final List<Pattern> EVENT_PATTERNS = compilePatterns(EVENT_KEYWORDS);
    private static final List<Pattern> NOTE_PATTERNS = compilePatterns(NOTE_KEYWORDS);
    private static final List<Pattern> TASK_PATTERNS = compilePatterns(TASK_KEYWORDS);

    public enum MessageType {
        EVENT,
        NOTE,
        TASK,
        UNKNOWN
    }

    private static List<Pattern> compilePatterns(String[] keywords) {
        List<Pattern> list = new ArrayList<>();
        for (String keyword : keywords) {
            list.add(Pattern.compile("(?U)\\b" + Pattern.quote(keyword.toLowerCase()) + "\\b"));
        }
        return list;
    }

    private static boolean containsPattern(String text, List<Pattern> patterns) {
        if (text == null || patterns == null) {
            return false;
        }
        String lower = text.toLowerCase();
        for (Pattern pattern : patterns) {
            if (pattern.matcher(lower).find()) {
                return true;
            }
        }
        return false;
    }

    public static MessageType detectType(String message) {
        if (containsPattern(message, EVENT_PATTERNS)) {
            return MessageType.EVENT;
        }
        if (containsPattern(message, TASK_PATTERNS)) {
            return MessageType.TASK;
        }
        if (containsPattern(message, NOTE_PATTERNS)) {
            return MessageType.NOTE;
        }
        return MessageType.UNKNOWN;
    }

    private final OpenRouterClient openRouterClient;

    public ChatEventLlmService(OpenRouterClient openRouterClient) {
        this.openRouterClient = openRouterClient;
    }

    // Public API

    public String detectSuggestionFromMessage(String messageContent) {
        if (!openRouterClient.isConfigured()) return "{}";

        if (!isWorthAnalyzing(messageContent)) {
            System.out.println("[LLM Chat] Đã chặn phân tích tin nhắn (spam, quá ngắn, v.v.): '" + messageContent + "'");
            return "{}";
        }

        MessageType messageType = detectType(messageContent);
        if (messageType == MessageType.UNKNOWN) {
            System.out.println("[LLM Chat] Không chứa từ khóa liên quan event/task/note, bỏ qua phân tích: '" + messageContent + "'");
            return "{}";
        }

        System.out.println("[LLM Chat] Đã phát hiện từ khóa loại " + messageType + " - Bắt đầu gọi LLM: '" + messageContent + "'");
        try {
            ZonedDateTime now = ZonedDateTime.now(BANGKOK_ZONE);
            String raw = callWithFallback(now, messageContent);
            return openRouterClient.parseJsonOrFallback(raw, "{}");
        } catch (Exception e) {
            System.err.println("[LLM Chat] Lỗi khi gọi OpenRouter / LLM: " + e.getMessage());
            return "{}";
        }
    }

    public static boolean containsEvent(String messageContent, String[] isEvent) {
        if (messageContent == null || isEvent == null) {
            return false;
        }
        Set<String> keywords = new HashSet<>();
        for (String word : isEvent) {
            keywords.add(word.toLowerCase());
        }

        // Sử dụng (?U)\\W+ để hỗ trợ Unicode (tiếng Việt), tránh bị tách sai các từ có dấu như "họp", "lịch"
        String[] words = messageContent.toLowerCase().split("(?U)\\W+");

        for (String word : words) {
            if (keywords.contains(word)) {
                return true;
            }
        }

        return false;
    }

    private String callWithFallback(ZonedDateTime now, String messageContent) throws Exception {
        Exception lastException = null;
        List<Map<String, Object>> messages = buildMessages(now, messageContent);

        for (String model : LlmPrompts.CHAT_EVENT_MODELS) {
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

    //  Pre-filter

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
