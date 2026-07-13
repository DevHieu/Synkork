package com.synkork.backend.common.utils.LLMFunction;

import java.util.List;

/**
 * Kho tập trung chứa toàn bộ cấu hình mô hình và prompt template dùng cho các LLM service.
 * <p>
 * Các service không nên định nghĩa prompt hay model ID riêng —
 * mọi thứ liên quan đến LLM đều đặt tại đây để dễ tra cứu và chỉnh sửa.
 * </p>
 */
public final class LlmPrompts {

    private LlmPrompts() {}

    public static final String APP_TITLE       = "Synkork";
    // TODO: Externalize to @ConfigurationProperties — hardcoded localhost won't work in production/staging.
    public static final String REFERER_CHAT    = "http://localhost:5173/rooms/chat";
    public static final String REFERER_DEFAULT = "http://localhost:5173";

    /** Danh sách model dự phòng cho phát hiện event/task/note, thử theo thứ tự. */
    public static final List<String> CHAT_EVENT_MODELS = List.of(
            "openai/gpt-oss-120b:free",
            "poolside/laguna-m.1:free",
            "nvidia/nemotron-3-super-120b-a12b:free",
            "z-ai/glm-4.5-air:free"
    );

    /** Model chuyển âm thanh cuộc họp thành văn bản. */
    public static final String MODEL_TRANSCRIPTION   = "nvidia/nemotron-3-nano-omni-30b-a3b-reasoning:free";

    /** Model tóm tắt nội dung cuộc họp. */
    public static final String MODEL_MEETING_SUMMARY = "openai/gpt-oss-120b:free";


    /**
     * System prompt phân loại ý định.
     */
    public static final String CHAT_EVENT_SYSTEM_PROMPT = """
Phân loại ý định tin nhắn chat nội bộ Synkork. Chỉ trả về JSON hợp lệ, không markdown.

Loại:
- EVENT: lịch hẹn, cuộc họp, sự kiện có mốc thời gian cố định.
- TASK: việc cần làm, nhắc việc, deadline, todo.
- NOTE: ghi lại thông tin để xem lại sau, không cần hẹn giờ hay hoàn thành.
- NONE: chào hỏi, cảm ơn, xác nhận, ký tự rác ("e", "ê", "eee", "tét"), không có ý định tạo mới.

Ưu tiên: EVENT > TASK > NOTE khi tin nhắn có nhiều ý.
Ngay cả câu kể ngắn ("mai có lịch họp", "có task mới", "có note") vẫn là ý định tạo mới — không mặc định về NONE.
Giữ ngôn ngữ gốc của người dùng. Không bịa thêm chi tiết. Chỉ điền field đúng loại; field còn lại để null/false.

Thời gian:
- Chuẩn hóa mọi biểu thức thời gian tương đối (hôm nay, mai, tối nay, thứ 2, cuối tuần, lúc 9h…) dựa vào ngày tham chiếu ở user prompt.
- eventDate / taskDueDate: yyyy-MM-dd. startTime / endTime: HH:mm (24h).
- Khoảng thời gian "9h-11h" → điền cả startTime lẫn endTime.
- Chỉ ngày, không giờ → field giờ để null. Không có ngày giờ → field thời gian để null.

Nội dung:
- title (EVENT): ngắn, tự nhiên. description: chỉ thêm ngữ cảnh, không lặp title.
- taskTitle: hành động ngắn gọn. taskDescription: chi tiết bổ sung nếu cần.
- noteTitle: tiêu đề ngắn. noteContent: nội dung cần lưu.

Cấu trúc JSON bắt buộc:
{
  "suggestionType": "EVENT|NOTE|TASK|NONE",
  "hasEvent": boolean,
  "hasNote": boolean,
  "hasTask": boolean,
  "title": string|null,
  "description": string|null,
  "eventDate": string|null,
  "startTime": string|null,
  "endTime": string|null,
  "noteTitle": string|null,
  "noteContent": string|null,
  "noteColor": string|null,
  "notePinned": boolean,
  "noteAllowEditAll": boolean,
  "taskTitle": string|null,
  "taskDescription": string|null,
  "taskColumnName": string|null,
  "taskDueDate": string|null
}
""";

    /**
     * User prompt gửi kèm tin nhắn cần phân loại.
     * Tham số theo thứ tự: currentDateTime, today, tomorrow, dayAfterTomorrow, messageContent.
     */
    public static final String CHAT_EVENT_USER_PROMPT_TEMPLATE = """
Múi giờ: Asia/Bangkok | Thời điểm hiện tại: %s
Quy đổi ngày: hôm nay=%s | mai=%s | ngày mốt=%s
Tin nhắn:
<user_input>
%s
</user_input>
""";

    // ═══════════════════════════════════════════════════════════════════════════
    // Meeting prompts — transcription & summarization
    // ═══════════════════════════════════════════════════════════════════════════

    /** Lệnh STT gửi kèm audio; không có tham số format. */
    public static final String MEETING_TRANSCRIPTION_INSTRUCTION =
            "Hãy chuyển âm thanh này thành văn bản tiếng Việt chính xác nhất. Chỉ trả về nội dung văn bản.";

    /**
     * Prompt tóm tắt cuộc họp; nhận 1 tham số: transcript.
     */
    public static final String MEETING_SUMMARY_PROMPT_TEMPLATE = """
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
            <user_input>
            %s
            </user_input>
            """;
}
