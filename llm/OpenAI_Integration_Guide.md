# Hướng dẫn chi tiết tích hợp LLM (OpenRouter & OpenAI) cho Synkork

Tài liệu này hướng dẫn chi tiết cách tích hợp các mô hình ngôn ngữ lớn (LLM) vào Synkork để thực hiện các tính năng thông minh như gợi ý sự kiện lịch và tóm tắt cuộc họp.

---

## PHẦN 1: TÍCH HỢP QUA OPENROUTER (Khuyên dùng)

OpenRouter là cổng trung gian cho phép gọi nhiều mô hình (GPT-4, Gemini, Claude...) qua một chuẩn API duy nhất.

### 1. Hướng dẫn lấy API Key từ OpenRouter
1. Truy cập [OpenRouter.ai](https://openrouter.ai/)
2. Đăng nhập và vào mục **Keys** (`https://openrouter.ai/keys`).
3. Nhấn **Create Key**, copy và lưu lại (bắt đầu bằng `sk-or-v1-`).

### 2. Cấu hình vào dự án
Thêm vào file `.env` (không commit file này):
```properties
OPENROUTER_API_KEY=your_key_here
OPENROUTER_BASE_URL=https://openrouter.ai/api/v1
```

### 3. Tính năng "Gợi ý tạo sự kiện từ tin nhắn"
**Quy trình:**
1. Backend nhận tin nhắn.
2. Gửi nội dung + thời gian hiện tại tới LLM kèm System Prompt.
3. LLM trả về JSON format: `{ "hasEvent": true, "title": "...", "eventDate": "YYYY-MM-DD", "startTime": "HH:mm" }`.
4. Backend gửi gợi ý về Frontend qua WebSocket.

---

## PHẦN 2: TÍCH HỢP TRỰC TIẾP OPENAI (Thay thế)

### 1. Chuẩn bị
- **API Key**: Lấy tại [OpenAI Dashboard](https://platform.openai.com/api-keys).
- Cấu hình `.env`: `OPENAI_API_KEY=your_openai_api_key_here`

### 2. Tóm tắt cuộc họp (Meeting Summary)
Quy trình: Audio -> Transcribe (Whisper) -> Summary (GPT).

**A. Backend Implementation (Java):**
```java
public String summarizeMeeting(MultipartFile audioFile) {
    // 1. Gửi audioFile đến OpenAI Whisper API -> Text
    // 2. Gửi text đó đến GPT với Prompt tóm tắt
    // 3. Trả về kết quả
}
```

**B. Frontend Integration:**
Gửi Blob ghi âm từ `recorder.onstop` lên Backend.

### 3. Gợi ý sự kiện Lịch
Sử dụng JSON Schema trong Prompt để đảm bảo LLM trả về đúng định dạng mong muốn.

---

## LƯU Ý QUAN TRỌNG
1. **Bảo mật**: Tuyệt đối không để lộ API Key trên GitHub.
2. **Chi phí**: Sử dụng các model như `gpt-4o-mini` hoặc `gemini-flash` để tối ưu chi phí và tốc độ.
3. **Async**: Luôn gọi LLM một cách bất đồng bộ để tránh làm treo ứng dụng.

---
*Tài liệu được cập nhật bởi Thai Hoc & Antigravity AI.*
