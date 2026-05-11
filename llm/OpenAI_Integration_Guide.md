# Hướng dẫn chi tiết tích hợp OpenRouter & Tạo Gợi ý Sự kiện cho Synkork

Tài liệu này hướng dẫn chi tiết cách lấy API Key từ OpenRouter, cấu hình vào dự án (`.env`), và cách xây dựng tính năng gợi ý tạo sự kiện khi người dùng nhắn tin (ví dụ: "mai 9h nhé").

## 1. Hướng dẫn lấy API Key từ OpenRouter

OpenRouter là một cổng trung gian cho phép bạn gọi nhiều mô hình LLM khác nhau (như OpenAI GPT, Google Gemini, Anthropic Claude, v.v.) qua chung một chuẩn API, giúp dễ dàng chuyển đổi model mà không cần thay đổi code.

**Các bước thực hiện:**
1. Truy cập trang web: [OpenRouter.ai](https://openrouter.ai/)
2. Đăng nhập hoặc tạo tài khoản mới (có thể đăng nhập bằng Google/GitHub).
3. Ở menu góc trên cùng bên phải, nhấp vào **Keys** (hoặc truy cập `https://openrouter.ai/keys`).
4. Nhấn nút **Create Key**. Bạn có thể đặt tên cho key này (ví dụ: `Synkork Backend`).
5. OpenRouter sẽ hiển thị API Key một lần duy nhất (bắt đầu bằng `sk-or-v1-`). Hãy copy và lưu lại đoạn mã này một cách an toàn.

## 2. Thêm API Key vào dự án (`.env`)

Sau khi có API Key, bạn cần cấu hình nó vào backend của dự án Synkork.

1. Mở thư mục `backend` của dự án.
2. Tìm file `.env` ở thư mục gốc của backend (hoặc tạo mới nếu chưa có).
3. Thêm các biến môi trường sau vào file `.env`:

```properties
# Cấu hình OpenRouter API
OPENROUTER_API_KEY=sk-or-v1-chuoi_api_key_ban_vua_copy_duoc
OPENROUTER_BASE_URL=https://openrouter.ai/api/v1
```

*Lưu ý quan trọng:* Tuyệt đối không commit file `.env` chứa khóa bảo mật này lên GitHub (hãy đảm bảo `.env` đã được liệt kê trong file `.gitignore`).

## 3. Tính năng "Gợi ý tạo sự kiện từ tin nhắn"

**Kịch bản:** Khi người dùng đang login (người gửi) gửi một tin nhắn như "mai 9h nhé", Backend sẽ gửi nội dung này tới OpenRouter để phân tích. LLM sẽ tính toán ra ngày mai là ngày nào, và 9h là giờ bắt đầu sự kiện. Sau đó Backend phản hồi về cho Frontend để hiển thị nút gợi ý.

### Bước 1: Chuẩn bị System Prompt cho LLM
Do LLM không tự biết ngày giờ hiện tại, ta phải truyền thời gian thực tế vào prompt để nó tính toán ra "mai" hay "tuần sau".

**System Prompt mẫu:**
```text
Bạn là một trợ lý ảo phân tích tin nhắn. Nhiệm vụ của bạn là tìm các cụm từ chỉ thời gian, ngày tháng và sự kiện trong đoạn chat.
Thời gian hiện tại của hệ thống: {current_datetime} (Ví dụ: 2026-05-06T14:30:00).
Nếu trong tin nhắn CÓ chứa ý định nhắc nhở, hẹn gặp, hoặc sự kiện lịch, hãy trả về JSON:
{
  "hasEvent": true,
  "title": "Tóm tắt tên sự kiện",
  "eventDate": "YYYY-MM-DD",
  "startTime": "HH:mm"
}
Nếu KHÔNG có ý định tạo sự kiện, chỉ cần trả về:
{
  "hasEvent": false
}
Không giải thích gì thêm, chỉ xuất kết quả JSON duy nhất.
```

### Bước 2: Gọi API OpenRouter ở Backend (Java)

Bạn có thể viết một Service để gọi tới OpenRouter (ví dụ dùng `RestTemplate` hoặc `WebClient`). Dưới đây là code tham khảo dùng `RestTemplate`:

```java
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class LlmService {

    @Value("${OPENROUTER_API_KEY}")
    private String openRouterApiKey;

    @Value("${OPENROUTER_BASE_URL}")
    private String openRouterBaseUrl;

    public String detectEventFromMessage(String messageContent) {
        RestTemplate restTemplate = new RestTemplate();
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openRouterApiKey);
        // headers.set("HTTP-Referer", "http://yourdomain.com"); // Khuyến nghị bởi OpenRouter
        // headers.set("X-Title", "Synkork"); 

        String prompt = "Bạn là trợ lý AI. Thời gian hiện tại là: " + LocalDateTime.now().toString() + ". "
                      + "Phân tích tin nhắn sau và xuất JSON format {hasEvent, title, eventDate, startTime}. "
                      + "Tin nhắn: \"" + messageContent + "\"";

        Map<String, Object> requestBody = new HashMap<>();
        // Bạn có thể chọn model miễn phí hoặc giá rẻ để test: google/gemini-flash-1.5, openai/gpt-4o-mini
        requestBody.put("model", "openai/gpt-4o-mini"); 
        
        Map<String, String> userMessage = new HashMap<>();
        userMessage.put("role", "user");
        userMessage.put("content", prompt);
        
        requestBody.put("messages", List.of(userMessage));
        // Yêu cầu trả về định dạng JSON
        requestBody.put("response_format", Map.of("type", "json_object")); 

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                openRouterBaseUrl + "/chat/completions",
                HttpMethod.POST,
                entity,
                String.class
        );

        // Kết quả sẽ chứa JSON do LLM sinh ra
        return response.getBody(); 
    }
}
```

### Bước 3: Tích hợp vào quy trình xử lý tin nhắn
Trong controller hay service xử lý thao tác gửi tin nhắn, bạn nên chạy việc phân tích này bằng một luồng **bất đồng bộ (Async)** để không làm chậm thao tác gửi tin nhắn.

```java
public MessageDTO saveMessage(MessageRequest request, Long senderId) {
    // 1. Lưu tin nhắn vào Database
    MessageEntity savedMessage = messageRepository.save(...);

    // 2. Chạy ngầm phân tích qua OpenRouter
    CompletableFuture.supplyAsync(() -> llmService.detectEventFromMessage(request.getContent()))
        .thenAccept(llmResponseJson -> {
            // Parse chuỗi JSON bằng ObjectMapper (Jackson)
            // Nếu "hasEvent" == true:
            //   Gửi sự kiện WebSocket (loại "EVENT_SUGGESTION") ngược lại cho senderId
            //   hoặc lưu thông tin suggestion vào DB để client poll về
        });

    return toDTO(savedMessage);
}
```

### Bước 4: Xử lý hiển thị ở Frontend (Vue 3)
Ở Frontend, khi bạn nhận được WebSocket event `EVENT_SUGGESTION` (hoặc response đính kèm):
1. Hiển thị một nút "Tạo sự kiện" (hoặc hiển thị Toast Notification). Ví dụ: *"Gợi ý tạo sự kiện: [Hẹn gặp] vào 09:00 ngày mai"*.
2. Khi người dùng click nút này, sử dụng composable của bạn (như mở modal/dialog thêm sự kiện Calendar) và truyền sẵn dữ liệu `title`, `eventDate`, `startTime` lấy được từ LLM.
3. Người dùng chỉ cần xem qua, sửa (nếu cần) và ấn **Lưu** để thêm thẳng vào Lịch cá nhân của họ trên Synkork.
