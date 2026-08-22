package com.synkork.backend.common.utils.LLMFunction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.multipart.MultipartFile;

import java.util.Base64;
import java.util.List;
import java.util.Map;

@Service
public class MeetingLlmService {

    private static final Logger log = LoggerFactory.getLogger(MeetingLlmService.class);


    private final OpenRouterClient openRouterClient;

    public MeetingLlmService(OpenRouterClient openRouterClient) {
        this.openRouterClient = openRouterClient;
    }

    /**
     * Cho phép controller kiểm tra sớm trước khi gọi LLM.
     */
    public boolean isConfigured() {
        return openRouterClient.isConfigured();
    }

    //Public API

    public String transcribeAudio(MultipartFile audioFile) throws Exception {
        if (!openRouterClient.isConfigured()) {
            throw new IllegalStateException("OPENROUTER_API_KEY chưa được cấu hình.");
        }

        byte[] bytes = audioFile.getBytes();
        String base64Audio = Base64.getEncoder().encodeToString(bytes);

        String format = audioFormat(audioFile);

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


    private String audioFormat(MultipartFile audioFile) {
        String contentType = audioFile.getContentType();
        if (contentType == null || !contentType.startsWith("audio/")) {
            throw new IllegalArgumentException("Định dạng audio không hợp lệ.");
        }
        contentType = contentType.split(";", 2)[0].trim().toLowerCase();

        return switch (contentType) {
            case "audio/webm" -> "webm";
            case "audio/mpeg" -> "mp3";
            case "audio/mp4", "audio/x-m4a" -> "m4a";
            case "audio/wav", "audio/x-wav" -> "wav";
            case "audio/ogg" -> "ogg";
            default -> throw new IllegalArgumentException("Định dạng audio chưa được hỗ trợ: " + contentType);
        };
    }

    private String summarizeWithPrompt(String prompt, List<String> models) {
        Exception lastException = null;
        List<Map<String, Object>> messages = List.of(
                Map.of("role", "user", "content", prompt));

        for (String model : models) {
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
                log.warn("Model {} thất bại, thử model dự phòng tiếp theo: {}", model, e.getMessage());
            } catch (Exception e) {
                lastException = e;
                log.warn("Lỗi không mong muốn với model {}: {}", model, e.getMessage());
            }
        }

        log.error("Tất cả các model đều thất bại", lastException);
        return "{}";
    }


    public String summarizeMeeting(String transcript) {
        if (transcript == null || transcript.isBlank()) {
            log.info("Bỏ qua tóm tắt: transcript rỗng hoặc chỉ chứa khoảng trắng.");
            return "{\"summary\":\"Nội dung không đủ để tóm tắt.\",\"keyPoints\":[],\"actionItems\":[]}";
        }

        String prompt = LlmPrompts.MEETING_SUMMARY_PROMPT_TEMPLATE.formatted(transcript);
        return summarizeWithPrompt(prompt, LlmPrompts.MEETING_SUMMARY_MODELS);
    }

    public String summarizeGeneric(String content,  List<String> models) {
        String promptTemplate = """
    ### Vai trò:
    Bạn là một trợ lý AI chuyên nghiệp tích hợp trong ứng dụng quản lý công việc. Nhiệm vụ của bạn là đọc tài liệu đầu vào và trích xuất thông tin sự kiện một cách chính xác.
    
    ### Chỉ thị nghiêm ngặt:
    1. Chỉ sử dụng thông tin có trong tài liệu đầu vào được cung cấp dưới đây. Tuyệt đối không tự suy diễn hoặc bịa đặt thông tin nằm ngoài tài liệu.
    2. Nếu tài liệu không đề cập đến một thông tin cụ thể nào đó trong định dạng yêu cầu, hãy để giá trị là "Không có thông tin".
    3. Trả về kết quả trực tiếp dưới dạng một đối tượng JSON hợp lệ. Không thêm bất kỳ câu dẫn nào trước hoặc sau JSON (ví dụ: KHÔNG viết "Dưới đây là kết quả JSON của bạn...").
    
    ### Định dạng đầu ra mong muốn (JSON):
    {
      "event_name": "Tên sự kiện hoặc tiêu đề cuộc họp",
      "time_location": "Thời gian và địa điểm diễn ra sự kiện (nếu có)",
      "summary": "Tóm tắt ngắn gọn 2-3 câu về nội dung chính của sự kiện",
      "action_items": [
        "Hành động 1 cần thực hiện (Người phụ trách - nếu có)",
        "Hành động 2 cần thực hiện (Người phụ trách - nếu có)"
      ]
    }
    
    ### Tài liệu đầu vào:
    %s
    """;
        String prompt = promptTemplate.formatted(content);
        return summarizeWithPrompt(prompt, models);
    }

}
