package com.synkork.backend.common.utils;

import com.synkork.backend.common.dtos.VoiceSummaryResponse;
import com.synkork.backend.common.utils.LLMFunction.MeetingLlmService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * Controller tạm thời dùng để test nhanh tính năng chuyển giọng nói thành văn bản và tóm tắt qua OpenRouter.
 * Endpoint này bắt đầu bằng /public nên không yêu cầu đăng nhập (JWT token),
 * giúp bạn có thể test trực tiếp bằng Postman hoặc cURL mà không cần chạy frontend.
 */
@RestController
@RequestMapping("/public/voice-summary")
public class VoiceSummaryTestController {

    private final MeetingLlmService meetingService;

    public VoiceSummaryTestController(MeetingLlmService meetingService) {
        this.meetingService = meetingService;
    }

    @PostMapping("/test-upload")
    public ResponseEntity<?> testUpload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("File rỗng.");
        }
        try {
            // 1) Chuyển audio thành transcript qua LLM
            String transcript = meetingService.transcribeAudio(file);
            
            // 2) Tóm tắt transcript vừa thu được
            String summaryJson = meetingService.summarizeMeeting(transcript);

            // 3) Trả về cấu trúc VoiceSummaryResponse hoàn chỉnh (Dùng URL mock để test nhanh)
            VoiceSummaryResponse response = new VoiceSummaryResponse(
                    "Xử lý thử nghiệm voice và tóm tắt thành công",
                    "http://mock-cloudinary-url.com/meeting-test.webm",
                    "mock-public-id-12345",
                    transcript,
                    summaryJson
            );

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            String detail = e.getMessage();
            if (e.getCause() != null) {
                detail += " | Cause: " + e.getCause().getMessage();
            }
            return ResponseEntity.status(500).body("Lỗi xử lý chuyển đổi & tóm tắt âm thanh (AI): " + detail);
        }
    }
}

