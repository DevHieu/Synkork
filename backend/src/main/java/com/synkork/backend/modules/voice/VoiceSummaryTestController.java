package com.synkork.backend.modules.voice;

import com.synkork.backend.modules.voice.dtos.VoiceSummaryResponse;
import com.synkork.backend.common.utils.LLMFunction.MeetingLlmService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

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
            String transcript = meetingService.transcribeAudio(file);
            String summaryJson = meetingService.summarizeMeeting(transcript);
            return ResponseEntity.ok(new VoiceSummaryResponse(
                    "Xử lý thử nghiệm voice và tóm tắt thành công",
                    null,
                    null,
                    transcript,
                    summaryJson
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Lỗi xử lý chuyển đổi & tóm tắt âm thanh (AI).");
        }
    }
}
