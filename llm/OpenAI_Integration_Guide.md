# Hướng dẫn tích hợp OpenAI API cho Synkork

Tài liệu này hướng dẫn cách tích hợp OpenAI vào dự án Synkork để thực hiện chức năng **Tóm tắt cuộc họp** và **Gợi ý sự kiện lịch từ tin nhắn**.

## 1. Chuẩn bị
- **API Key**: Lấy tại [OpenAI Dashboard](https://platform.openai.com/api-keys).
- **Thư viện Backend (Spring Boot)**: Khuyên dùng `Spring AI` hoặc `OpenAI Java SDK`.
- **Thư viện Frontend (Vue.js)**: Sử dụng `axios` để gọi về Backend (không nên gọi trực tiếp OpenAI từ Frontend để bảo mật API Key).

## 2. Cấu hình Môi trường
Thêm API Key vào file `.env` ở cả Backend và Frontend:

**Backend (`backend/.env`):**
```properties
OPENAI_API_KEY=your_openai_api_key_here
```

## 3. Tóm tắt cuộc họp (Meeting Summary)
Quy trình: Audio -> Transcribe (Whisper) -> Summary (GPT).

### A. Backend Implementation (Java)
Bạn nên tạo một `LlmService.java` trong `backend/src/main/java/com/synkork/backend/common/utils/llm`.

```java
// Ví dụ logic gọi OpenAI Whisper và GPT
public String summarizeMeeting(MultipartFile audioFile) {
    // 1. Gửi audioFile đến OpenAI Whisper API để lấy text (Transcription)
    // 2. Gửi text đó đến GPT-4o-mini với Prompt tóm tắt
    String prompt = "Hãy tóm tắt cuộc họp sau đây thành các ý chính và hành động cần làm (Action Items): " + transcription;
    // 3. Trả về kết quả tóm tắt
}
```

### B. Frontend Integration (`VoiceHeader.vue`)
Sửa đổi hàm `handleSummary` để gửi file ghi âm lên backend:

```typescript
recorder.onstop = async () => {
  const blob = new Blob(chunks, { type: "audio/webm" });
  const formData = new FormData();
  formData.append("audio", blob);

  // Gọi API backend xử lý tóm tắt
  const response = await axios.post('/api/ai/summarize-meeting', formData);
  const summary = response.data; // Nội dung tóm tắt từ LLM
  
  // Hiển thị tóm tắt hoặc lưu vào Note
};
```

## 4. Gợi ý sự kiện Lịch (Calendar Suggestion)
Quy trình: Tin nhắn -> LLM (Detection) -> JSON Schema -> Frontend Dialog.

### A. Prompt cho LLM
Sử dụng Prompt để yêu cầu LLM trả về định dạng JSON cố định:

```text
Phân tích tin nhắn sau và trích xuất thông tin sự kiện nếu có. 
Nếu không có sự kiện, trả về null. 
Nếu có, trả về JSON theo format: 
{
  "title": "tên sự kiện",
  "eventDate": "YYYY-MM-DD",
  "startTime": "HH:mm",
  "endTime": "HH:mm",
  "description": "mô tả"
}
Tin nhắn: "Chúng ta có buổi họp team vào 9h sáng thứ Hai tới nhé, khoảng 1 tiếng."
```

### B. Tích hợp Backend (`MessageService.java`)
Trong hàm `saveMessage`, gửi nội dung tin nhắn qua LLM để kiểm tra:

```java
public MessageDTO saveMessage(MessageDTO dto, String senderId) {
    MessageEntity message = messageRepository.save(entity);
    
    // Gọi LLM kiểm tra ý định tạo lịch (Async để không chậm tốc độ chat)
    LlmSuggestion suggestion = llmService.detectEvent(dto.getContent());
    if (suggestion != null) {
        // Gửi qua WebSocket cho người dùng thấy gợi ý
    }
    return dto;
}
```

## 5. Lưu ý quan trọng
1. **Bảo mật**: Tuyệt đối không để lộ `OPENAI_API_KEY` trên GitHub. Đã cấu hình `.gitignore` bỏ qua file `.env`.
2. **Chi phí**: Sử dụng model `gpt-4o-mini` để tiết kiệm chi phí và có tốc độ phản hồi nhanh.
3. **Audio Format**: OpenAI Whisper hỗ trợ tốt nhất định dạng `.mp3`, `.wav`, hoặc `.webm`.

---
*Tài liệu này được tạo tự động bởi Antigravity AI để hỗ trợ phát triển dự án Synkork.*
