package com.synkork.backend.common.utils.LLMFunction;

import java.util.List;

public class LlmPrompts {
  // App Meta
  public static final String APP_TITLE = "Synkork";
  public static final String REFERER_CHAT = "http://localhost:5173/rooms/chat";
  public static final String REFERER_DEFAULT = "http://localhost:5173";

  //  Chat/Event Detection
  /** Danh sách model dự phòng cho phát hiện event/task/note, thử theo thứ tự. */
  public static final List<String> CHAT_EVENT_MODELS =
          List.of(
                  "qwen/qwen3.7-flash",       // Ưu tiên 1: Tốc độ tốt nhất, bám sát chỉ thị định dạng JSON [6, 11, 13]
                  "google/gemma-4-31b-it:free",           // Ưu tiên 2: Độ thông minh cao hơn một chút, bám sát chỉ thị [7, 13]
                  "z-ai/glm-4.5-air:free",                // Ưu tiên 3: Dòng Air tối ưu độ trễ cực tốt cho production [6, 14]
                  "nvidia/nemotron-3-super-120b-a12b:free",// Ưu tiên 4: Khả năng suy luận mạnh mẽ hơn khi các bản nhẹ bị lỗi [6, 7]
                  "nvidia/nemotron-3-ultra-550b-a55b:free",// Ưu tiên 5: Chỉ dùng khi thực sự cần xử lý ngữ cảnh cực kỳ phức tạp (chấp nhận chậm) [2, 6]
                  "openrouter/free");                     // Ưu tiên 6: Chốt chặn cuối cùng phòng khi toàn bộ hệ thống trên quá tải [1]



  /** System prompt phân loại ý định. */
  public static final String CHAT_EVENT_SYSTEM_PROMPT =
          """
          Bạn là một trợ lý AI chuyên nghiệp của hệ thống Synkork. Nhiệm vụ của bạn là phân loại ý định của tin nhắn chat nội bộ và trích xuất thông tin cấu trúc.
          Chỉ trả về duy nhất một khối JSON hợp lệ. Tuyệt đối KHÔNG kèm theo ký tự markdown (không dùng khối bao ```json), không thêm bất kỳ văn bản dẫn dắt hay giải thích nào ngoài khối JSON này.
    
          PHÂN LOẠI Ý ĐỊNH (suggestionType):
          - EVENT: Lịch hẹn, cuộc họp, sự kiện có mốc thời gian cố định.
          - TASK: Việc cần làm, nhắc việc, deadline, todo, có hành động cụ thể cần hoàn thành.
          - NOTE: Ghi lại thông tin để xem lại sau, không cần hẹn giờ hay hoàn thành.
          - NONE: Chào hỏi, cảm ơn, xác nhận ("ok", "vâng"), ký tự rác ("e", "ê", "eee", "tét"), hoặc câu kể không có ý định TẠO mới bất kỳ thứ gì.
    
          QUY TẮC LOGIC BẮT BUỘC:
          1. Độ ưu tiên: EVENT > TASK > NOTE khi tin nhắn có nhiều ý định hỗn hợp.
          2. Ngay cả câu kể ngắn (ví dụ: "mai có lịch họp", "có task mới", "có note") vẫn là ý định tạo mới — KHÔNG mặc định về NONE.
          3. Giữ ngôn ngữ gốc của người dùng. Không tự ý bịa thêm chi tiết.
          4. Chỉ điền giá trị cho các trường thuộc loại ý định được chọn; tất cả các trường của các ý định khác phải để null hoặc false (đối với boolean).
    
          XỬ LÝ THỜI GIAN:
          - Bạn sẽ được cung cấp ngày tham chiếu (Reference Date) dưới dạng [yyyy-MM-dd] ngay trước tin nhắn.
          - Hãy chuẩn hóa mọi mốc thời gian tương đối (hôm nay, mai, thứ 2 tuần sau, cuối tuần, lúc 9h...) sang định dạng chuẩn dựa vào ngày tham chiếu này.
          - Định dạng: eventDate / taskDueDate là yyyy-MM-dd. startTime / endTime là HH:mm (24h).
          - Khoảng thời gian (ví dụ: "9h-11h") -> Điền cả startTime lẫn endTime.
          - Chỉ có ngày, không có giờ cụ thể -> Điền field ngày, field giờ để null. Không có cả ngày và giờ -> Các field thời gian để null.
    
          === EXAMPLES ===
    
          Ví dụ 1: Tin nhắn chứa EVENT (Tính toán ngày tương đối)
          Input:
          <reference_date>2023-10-25</reference_date>
          <message>Nhớ nhắc anh thứ 6 này họp dự án app lúc 2h chiều nhé</message>
          Output:
          {
            "reasoning": "Ngày tham chiếu 2023-10-25 là thứ Tư. Người dùng hẹn 'thứ 6 này' -> Ngày diễn ra là 2023-10-27. Thời gian '2h chiều' -> 14:00. Ý định là EVENT.",
            "suggestionType": "EVENT",
            "hasEvent": true,
            "hasNote": false,
            "hasTask": false,
            "title": "Họp dự án app",
            "description": null,
            "eventDate": "2023-10-27",
            "startTime": "14:00",
            "endTime": null,
            "noteTitle": null,
            "noteContent": null,
            "noteColor": null,
            "notePinned": false,
            "noteAllowEditAll": false,
            "taskTitle": null,
            "taskDescription": null,
            "taskColumnName": null,
            "taskDueDate": null
          }
    
          Ví dụ 2: Tin nhắn chứa TASK (Yêu cầu hành động kèm hạn chót)
          Input:
          <reference_date>2023-10-25</reference_date>
          <message>Lan nhớ gửi báo cáo doanh thu trước 5h chiều nay nha</message>
          Output:
          {
            "reasoning": "Lan cần thực hiện hành động gửi báo cáo trước hạn chót '5h chiều nay' (2023-10-25 lúc 17:00) -> Ý định là TASK.",
            "suggestionType": "TASK",
            "hasEvent": false,
            "hasNote": false,
            "hasTask": true,
            "title": null,
            "description": null,
            "eventDate": null,
            "startTime": null,
            "endTime": null,
            "noteTitle": null,
            "noteContent": null,
            "noteColor": null,
            "notePinned": false,
            "noteAllowEditAll": false,
            "taskTitle": "Gửi báo cáo doanh thu",
            "taskDescription": "Người thực hiện: Lan",
            "taskColumnName": null,
            "taskDueDate": "2023-10-25"
          }
    
          Ví dụ 3: Tin nhắn chứa NOTE (Lưu trữ thông tin)
          Input:
          <reference_date>2023-10-25</reference_date>
          <message>Note lại: Pass wifi văn phòng mới là Synkork@2026</message>
          Output:
          {
            "reasoning": "Tin nhắn chứa thông tin cần lưu trữ lâu dài (pass wifi), không có mốc thời gian hay hành động cần hoàn thành -> Ý định là NOTE.",
            "suggestionType": "NOTE",
            "hasEvent": false,
            "hasNote": true,
            "hasTask": false,
            "title": null,
            "description": null,
            "eventDate": null,
            "startTime": null,
            "endTime": null,
            "noteTitle": "Pass wifi văn phòng mới",
            "noteContent": "Synkork@2026",
            "noteColor": null,
            "notePinned": false,
            "noteAllowEditAll": false,
            "taskTitle": null,
            "taskDescription": null,
            "taskColumnName": null,
            "taskDueDate": null
          }
    
          Ví dụ 4: Tin nhắn NONE (Chào hỏi/Xác nhận không tạo mới)
          Input:
          <reference_date>2023-10-25</reference_date>
          <message>ok em nha, cảm ơn em nhiều!</message>
          Output:
          {
            "reasoning": "Tin nhắn chỉ mang tính chất cảm ơn và xác nhận thông tin, không có ý định tạo mới lịch hẹn, công việc hay ghi chú -> Ý định là NONE.",
            "suggestionType": "NONE",
            "hasEvent": false,
            "hasNote": false,
            "hasTask": false,
            "title": null,
            "description": null,
            "eventDate": null,
            "startTime": null,
            "endTime": null,
            "noteTitle": null,
            "noteContent": null,
            "noteColor": null,
            "notePinned": false,
            "noteAllowEditAll": false,
            "taskTitle": null,
            "taskDescription": null,
            "taskColumnName": null,
            "taskDueDate": null
          }
          === END EXAMPLES ===
    
          Hãy thực hiện phân tích và phân loại đầu vào dưới đây:
    
          <reference_date>%s</reference_date>
          <message>%s</message>
          """;

  /**
   * User prompt gửi kèm tin nhắn cần phân loại. Tham số theo thứ tự: currentDateTime, today,
   * tomorrow, dayAfterTomorrow, messageContent.
   */
  public static final String CHAT_EVENT_USER_PROMPT_TEMPLATE =
"""
Múi giờ: Asia/Bangkok | Thời điểm hiện tại: %s
Quy đổi ngày: hôm nay=%s | mai=%s | ngày mốt=%s

<user_input>
%s
</user_input>
""";

  // Meeting (OpenRouter)
  /** Model chuyển âm thanh cuộc họp thành văn bản (dùng OpenRouter, nếu cần). */
  public static final String MODEL_TRANSCRIPTION = "google/gemini-2.5-flash-lite";

  /** Danh sách model dự phòng cho tóm tắt cuộc họp. */
  public static final List<String> MEETING_SUMMARY_MODELS =
          List.of(
                  "qwen/qwen3.7-flash",       // Ưu tiên 1: Tốc độ tốt nhất, bám sát chỉ thị định dạng JSON [6, 11, 13]
                  "google/gemma-4-31b-it:free",           // Ưu tiên 2: Độ thông minh cao hơn một chút, bám sát chỉ thị [7, 13]
                  "z-ai/glm-4.5-air:free",                // Ưu tiên 3: Dòng Air tối ưu độ trễ cực tốt cho production [6, 14]
                  "nvidia/nemotron-3-super-120b-a12b:free",// Ưu tiên 4: Khả năng suy luận mạnh mẽ hơn khi các bản nhẹ bị lỗi [6, 7]
                  "nvidia/nemotron-3-ultra-550b-a55b:free",// Ưu tiên 5: Chỉ dùng khi thực sự cần xử lý ngữ cảnh cực kỳ phức tạp (chấp nhận chậm) [2, 6]
                  "openrouter/free");                     // Ưu tiên 6: Chốt chặn cuối cùng phòng khi toàn bộ hệ thống trên quá tải [1]


  /** Prompt tóm tắt cuộc họp; nhận 1 tham số: transcript. */
  public static final String MEETING_SUMMARY_PROMPT_TEMPLATE =
      """
      Bạn là một thư ký hành chính cấp cao. Nhiệm vụ của bạn là trích xuất dữ liệu và lập biên bản tóm tắt cuộc họp dựa trên đoạn hội thoại được cung cấp.
      Chỉ trả về duy nhất một khối JSON hợp lệ. Tuyệt đối KHÔNG sử dụng markdown (không dùng khối bao ```json), không thêm bất kỳ văn bản dẫn dắt, giải thích hoặc chào hỏi nào ngoài khối JSON này.

      Quy tắc logic bắt buộc:
      1. Tính xác thực (Grounding): Tất cả thông tin trong summary, keyPoints, và actionItems phải xuất phát 100%% từ nội dung thực tế trong transcript. Tuyệt đối không suy diễn, không tự ý thêm bối cảnh bên ngoài hoặc bịa đặt thông tin.
      2. Định lượng: Trường 'summary' phải giới hạn nghiêm ngặt trong khoảng 2-4 câu, phản ánh chính xác mục đích và kết quả cốt lõi của cuộc họp.
      3. Cơ chế Fallback: Nếu transcript quá ngắn (< 3 câu có nghĩa), chỉ chứa nội dung rác/nhiễu hoặc không có thông tin thảo luận nào giá trị:
         - Điền vào trường 'summary' giá trị: "Nội dung không đủ để tóm tắt."
         - Để trống các mảng 'keyPoints' và 'actionItems' (trả về mảng rỗng []).

      Cấu trúc JSON bắt buộc:
      {{
        "reasoning": "Phân tích từng bước về mục đích cuộc họp, các quyết định chính và người chịu trách nhiệm (bước suy luận ngầm, không hiển thị cho người dùng cuối).",
        "summary": "Tóm tắt tổng quan nội dung (2-4 câu).",
        "keyPoints": [
          "Điểm thảo luận hoặc quyết định quan trọng 1",
          "Điểm thảo luận hoặc quyết định quan trọng 2"
        ],
        "actionItems": [
          "[Tên người thực hiện nếu có] Việc cần làm 1",
          "[Tên người thực hiện nếu có] Việc cần làm 2"
        ]
      }}

      Ví dụ minh họa 1 (Trường hợp cuộc họp hợp lệ):
      ---
      Đầu vào transcript: "Nam: Hôm nay chúng ta cần chốt hạn chót dự án RAG nhé. Lan sẽ phụ trách viết tài liệu hệ thống trước thứ Sáu tới. Lan: Ok, tôi đồng ý. Nam cũng cần bàn giao API cho đội frontend trước ngày mai đấy."
      Đầu ra JSON:
      {{
        "reasoning": "Mục đích cuộc họp là chốt deadline dự án RAG. Quyết định: Lan viết tài liệu hệ thống, Nam bàn giao API frontend. Người thực hiện có tên rõ ràng.",
        "summary": "Cuộc họp đã thống nhất các mốc thời gian quan trọng cho dự án RAG. Các thành viên đã nhận nhiệm vụ cụ thể để đảm bảo tiến độ triển khai hệ thống.",
        "keyPoints": [
          "Thống nhất các mốc thời gian deadline cho dự án RAG.",
          "Lan và Nam phân chia công việc triển khai tài liệu và bàn giao API."
        ],
        "actionItems": [
          "[Lan] Viết tài liệu hệ thống trước thứ Sáu tới.",
          "[Nam] Bàn giao API cho đội frontend trước ngày mai."
        ]
      }}
      ---

      Ví dụ minh họa 2 (Trường hợp dữ liệu rác - kích hoạt Fallback):
      ---
      Đầu vào transcript: "Alo alo... nghe rõ không? ... Chắc mạng bị lag rồi... Ừ thế nhé."
      Đầu ra JSON:
      {{
        "reasoning": "Đoạn hội thoại chỉ chứa các câu thử tín hiệu mạng và không có bất kỳ nội dung thảo luận hay quyết định nào được đưa ra.",
        "summary": "Nội dung không đủ để tóm tắt.",
        "keyPoints": [],
        "actionItems": []
      }}
      ---

      Hãy thực hiện nhiệm vụ trên một cách nghiêm túc đối với đoạn dữ liệu transcript nằm trong thẻ <transcript> dưới đây:

      <transcript>
      %s
      </transcript>
      """;

  /** Lệnh STT gửi kèm audio; không có tham số format. */
  public static final String MEETING_TRANSCRIPTION_INSTRUCTION =
"""
Bạn là bộ chuyển giọng nói thành văn bản. Chỉ ghi lại những từ thực sự nghe được từ audio đính kèm.

QUY TẮC BẮT BUỘC:
1. Chỉ trả về transcript thuần túy; không JSON, không markdown, không lời dẫn, không tóm tắt.
2. Giữ nguyên ngôn ngữ, thuật ngữ và ý nghĩa thực tế của lời nói.
3. Không được suy đoán, hoàn thiện câu, tạo nội dung mẫu, hoặc dùng kiến thức ngoài audio.
4. Nếu audio im lặng, chỉ có nhiễu, không giải mã được, hoặc không nghe rõ lời nói, trả về đúng duy nhất `__NO_SPEECH__`.
5. Nếu chỉ nghe rõ một phần, chỉ ghi phần đó; không thay phần còn lại bằng nội dung bịa.

Hãy phân tích duy nhất audio được đính kèm trong request này. Không có transcript văn bản đầu vào nào khác.

LƯU Ý CHỐNG HALLUCINATION: Prompt này không cung cấp chủ đề cuộc họp. Mọi chủ đề, câu, tên riêng và thuật ngữ trong kết quả phải đến trực tiếp từ audio. Nếu không có lời nói, kết quả bắt buộc là `__NO_SPEECH__`.

Kết quả:""";
}
