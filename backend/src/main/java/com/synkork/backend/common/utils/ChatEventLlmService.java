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
    private static final String EVENT_MODEL = "moonshotai/kimi-k2.6:free";

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

Luật nhận diện thời gian:
- Phải chú ý mọi biểu thức chỉ thời gian hoặc hạn chót, kể cả tuyệt đối và tương đối.
- Ví dụ thời gian tương đối: hôm nay, mai, ngày mai, ngày mốt, tối nay, sáng mai, chiều mai, tuần sau, thứ 2, thứ ba tới, cuối tuần, cuối tháng, đầu giờ chiều, trước 5h, sau 2 tiếng, lúc 9h, 9:30, 14h, 14:30.
- Nếu tin nhắn có thời gian rõ ràng hoặc đủ rõ để suy ra theo thời điểm tham chiếu, hãy chuẩn hóa vào các field thời gian.
- Nếu tin nhắn chứa "mai" hoặc "ngày mai", phải hiểu chính xác là ngày tham chiếu cộng 1 ngày; không được dùng ngày hôm nay.
- Nếu tin nhắn chứa "ngày mốt", phải hiểu chính xác là ngày tham chiếu cộng 2 ngày.
- eventDate và taskDueDate phải dùng định dạng yyyy-MM-dd.
- startTime và endTime phải dùng định dạng HH:mm theo 24 giờ.
- Nếu chỉ có ngày mà chưa có giờ, vẫn tạo EVENT hoặc TASK nếu ý định rõ ràng; field ngày phải có giá trị, field giờ để null.
- Nếu chỉ có giờ mà không nói ngày nhưng có thể suy ra từ ngữ cảnh như "tối nay", "mai 9h", "chiều thứ 2", hãy suy ra ngày từ thời điểm tham chiếu.
- Nếu có khoảng thời gian như "từ 9h đến 11h", "9h-11h", "2pm đến 4pm", điền cả startTime và endTime.
- Nếu là TASK có hạn chót như "trước 5h", "deadline mai", "xong trước thứ 6", ưu tiên TASK và điền taskDueDate nếu suy ra được ngày; nếu suy ra được giờ thì đưa giờ đó vào taskDescription một cách ngắn gọn, không tạo field ngoài schema.
- Nếu tin nhắn chủ yếu là lịch họp/lịch hẹn/lịch gặp theo thời gian, ưu tiên EVENT.
- Nếu tin nhắn chủ yếu là việc phải làm dù có thời gian đi kèm, ưu tiên TASK.
- Không được bỏ sót thời gian khi người dùng đã nêu rõ hoặc ngầm nêu đủ rõ.

Quy tắc tạo nội dung:
- title dùng cho EVENT phải ngắn, tự nhiên, mô tả đúng cuộc hẹn/sự kiện.
- description dùng cho EVENT chỉ bổ sung ngữ cảnh cần thiết, không lặp lại title.
- taskTitle phải là hành động cần làm, rõ chủ ngữ ngầm và ngắn gọn.
- taskDescription chỉ chứa chi tiết bổ sung hữu ích như hạn chót theo giờ, đối tượng liên quan, hoặc bối cảnh.
- noteTitle là tiêu đề ngắn gọn của ghi chú; noteContent chứa nội dung cần lưu.

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
- "chiều thứ 2 họp sprint planning" -> EVENT, cần suy ra đúng eventDate; nếu không có giờ chính xác thì startTime để null
- "9h-11h mai review thiết kế" -> EVENT
- "ghi lại ý này giúp tôi" -> NOTE
- "note: tên domain là synkork.vn" -> NOTE
- "tạo task nhắc mình gửi báo cáo" -> TASK
- "nhắc mình mua sữa tối nay" -> TASK
- "xử lý bug đăng nhập trước 5h" -> TASK
- "hoàn thành proposal trước thứ 6" -> TASK
- "deadline mai 15h nộp báo cáo" -> TASK
- "ok cảm ơn" -> NONE
- "chỉ mình cái này nhé" -> NONE

Ngày tham chiếu:
- hôm nay: %s
- ngày mai: %s
- ngày mốt: %s
""";

    private static final String SUGGESTION_USER_PROMPT_TEMPLATE = """
Múi giờ tham chiếu: Asia/Bangkok
Thời điểm hiện tại: %s
Quy đổi ngày bắt buộc:
- hôm nay = %s
- mai / ngày mai = %s
- ngày mốt = %s
Khi tin nhắn có các cụm trên, phải dùng đúng ngày đã quy đổi ở đây để điền eventDate hoặc taskDueDate.
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
        String today = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String tomorrow = now.plusDays(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String dayAfterTomorrow = now.plusDays(2).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        return SUGGESTION_USER_PROMPT_TEMPLATE.formatted(
                currentTime,
                today,
                tomorrow,
                dayAfterTomorrow,
                messageContent
        );
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
