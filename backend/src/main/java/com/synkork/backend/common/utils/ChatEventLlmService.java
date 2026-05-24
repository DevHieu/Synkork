package com.synkork.backend.common.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Service
public class ChatEventLlmService {
    private static final ZoneId BANGKOK_ZONE = ZoneId.of("Asia/Bangkok");

    private static final String CHAT_REFERER = "http://localhost:5173/rooms/chat";
    private static final String APP_TITLE = "Synkork";
    private static final String EVENT_MODEL = "liquid/lfm-2.5-1.2b-thinking:free";

    private final ObjectMapper objectMapper;
    private final OpenRouterClient openRouterClient;

    public ChatEventLlmService(ObjectMapper objectMapper, OpenRouterClient openRouterClient) {
        this.objectMapper = objectMapper;
        this.openRouterClient = openRouterClient;
    }

    private static final String SUGGESTION_SYSTEM_PROMPT = """
Bạn là bộ phân loại ý định cho tin nhắn chat nội bộ của Synkork.
Nhiệm vụ của bạn là phát hiện tin nhắn có liên quan đến sự kiện, ghi chú, hoặc task.

Chỉ trả về JSON hợp lệ, không markdown, không giải thích thêm.

Mục tiêu:
- EVENT: người dùng muốn tạo/đặt lịch một sự kiện, cuộc hẹn, cuộc họp, cuộc gặp, cuộc gọi, buổi đi chơi, hoặc một mốc thời gian cố định.
- NOTE: người dùng muốn ghi lại, lưu lại, tóm tắt, note, đánh dấu một thông tin để xem lại sau.
- TASK: người dùng muốn tạo việc cần làm, nhắc việc, giao việc, checklist, todo, hoặc công việc cần xử lý.

Luật quyết định:
- Chọn EVENT khi trọng tâm là thời điểm diễn ra một sự kiện.
- Chọn TASK khi trọng tâm là hành động cần hoàn thành.
- Chọn NOTE khi trọng tâm chỉ là lưu thông tin, không cần hẹn giờ hay trạng thái hoàn thành.
- Nếu một tin nhắn có nhiều ý, chọn loại mạnh nhất theo thứ tự: EVENT > TASK > NOTE.
- Nếu người dùng chỉ chào hỏi, cảm ơn, xác nhận, hoặc nhắc đến từ khóa nhưng không có ý định tạo mới, trả về NONE.
- Nếu không chắc chắn, trả về NONE.
- Giữ nguyên ngôn ngữ người dùng; nếu người dùng viết tiếng Việt thì title, description, noteTitle, taskTitle cũng phải tự nhiên bằng tiếng Việt.
- Không bịa thêm chi tiết không có trong tin nhắn.
- Chỉ điền field đúng với loại đã chọn; các field còn lại để null hoặc false.

Cấu trúc bắt buộc:
{
  "suggestionType":"EVENT|NOTE|TASK|NONE",
  "hasEvent":boolean,
  "hasNote":boolean,
  "hasTask":boolean,
  "title":string|null,
  "description":string|null,
  "eventDate":string|null,
  "startTime":string|null,
  "endTime":string|null,
  "noteTitle":string|null,
  "noteContent":string|null,
  "noteColor":string|null,
  "notePinned":boolean,
  "noteAllowEditAll":boolean,
  "taskTitle":string|null,
  "taskDescription":string|null,
  "taskColumnName":string|null,
  "taskDueDate":string|null
}
""";

    private static final String SUGGESTION_EXAMPLES_TEMPLATE = """
Ví dụ nhận diện:
- "mai 9h họp team" -> EVENT
- "thứ 6 này đặt lịch gặp khách hàng" -> EVENT
- "ghi lại ý này giúp tôi" -> NOTE
- "note: tên domain là synkork.vn" -> NOTE
- "tạo task nhắc mình gửi báo cáo" -> TASK
- "nhắc mình mua sữa tối nay" -> TASK
- "xử lý bug đăng nhập trước 5h" -> TASK
- "ok cảm ơn" -> NONE
- "chỉ mình cái này nhé" -> NONE

Ngày tham chiếu:
- hôm nay: %s
- ngày mai: %s
- ngày mốt: %s
""";

    private static final String SUGGESTION_USER_PROMPT_TEMPLATE = """
Thời điểm tham chiếu: %s
Tin nhắn: %s
""";

    public String detectSuggestionFromMessage(String messageContent) {
        // 1) Trả về sớm nếu OpenRouter chưa được cấu hình.
        if (!openRouterClient.isConfigured()) {
            return "{}";
        }

        try {
            // 2) Dựng request có kèm thời gian hiện tại theo múi giờ Bangkok.
            ZonedDateTime now = ZonedDateTime.now(BANGKOK_ZONE);
            String rawResult = openRouterClient.chatCompletion(
                    CHAT_REFERER,
                    APP_TITLE,
                    EVENT_MODEL,
                    buildSuggestionMessages(now, messageContent),
                    true
            );
            // 3) Giữ kiểm tra cục bộ và đơn giản: chỉ nhận JSON hợp lệ.
            return parseJsonOrFallback(rawResult);
        } catch (Exception e) {
            return "{}";
        }
    }

    public String detectEventFromMessage(String messageContent) {
        return detectSuggestionFromMessage(messageContent);
    }

    private List<Map<String, Object>> buildSuggestionMessages(ZonedDateTime now, String messageContent) {
        // Dựng lại các giá trị phụ thuộc thời gian ở đây để model tự suy luận ngày giờ.
        String systemPrompt = buildSuggestionSystemPrompt(now);
        String userPrompt = buildSuggestionUserPrompt(now, messageContent);

        return List.of(
                Map.of("role", "system", "content", systemPrompt),
                Map.of("role", "user", "content", userPrompt)
        );
    }

    private String buildSuggestionSystemPrompt(ZonedDateTime now) {
        String today = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String tomorrow = now.plusDays(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String dayAfterTomorrow = now.plusDays(2).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        return SUGGESTION_SYSTEM_PROMPT + "\n" + SUGGESTION_EXAMPLES_TEMPLATE.formatted(today, tomorrow, dayAfterTomorrow);
    }

    private String buildSuggestionUserPrompt(ZonedDateTime now, String messageContent) {
        String currentTime = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        return SUGGESTION_USER_PROMPT_TEMPLATE.formatted(currentTime, messageContent);
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
