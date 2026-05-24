package com.synkork.backend.common.utils;

import com.synkork.backend.common.dtos.FileUploaded;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/collaboration/voice-summary")
public class VoiceSummaryController {
    // Controller này chỉ nối luồng upload -> chuyển giọng nói thành văn bản -> tóm tắt.
    private final MeetingLlmService meetingService;
    private final FileService fileService;

    public VoiceSummaryController(MeetingLlmService meetingService, FileService fileService) {
        this.meetingService = meetingService;
        this.fileService = fileService;
    }

    @PostMapping("/upload")
    public ResponseEntity<?> uploadVoice(
            @RequestParam("file") MultipartFile file,
            @RequestParam("userId") String userId,
            @RequestParam("userName") String userName,
            @RequestParam("roomId") String roomId) {

        try {
            // userId và userName vẫn được giữ trong contract để tương thích với bên gọi.
            // 1) Lưu file upload trước để frontend nhận được URL ổn định.
            FileUploaded uploaded = fileService.uploadFile(file, "synkork/voice-notes/" + roomId);
            String fileUrl = uploaded.url();
            String publicId = uploaded.publicId();

            // 2) Chuyển audio thành transcript, rồi tóm tắt transcript.
            String transcript = meetingService.transcribeAudio(file);
            String summaryJson = meetingService.summarizeMeeting(transcript);

            // 3) Trả về đầy đủ payload để bên gọi dùng lại cả transcript lẫn summary.
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Xử lý voice thành công");
            response.put("fileUrl", fileUrl);
            response.put("publicId", publicId);
            response.put("transcript", transcript);
            response.put("analysis", summaryJson);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Lỗi xử lý file voice: " + e.getMessage());
        }
    }
}
