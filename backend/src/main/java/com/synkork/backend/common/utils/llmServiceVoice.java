package com.synkork.backend.common.utils;

import com.synkork.backend.common.dtos.FileUploaded;
import org.springframework.beans.factory.annotation.Autowired;
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
public class llmServiceVoice {

    @Autowired
    private llmMeetingService meetingService;

    @Autowired
    private FileService fileService;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadVoice(
            @RequestParam("file") MultipartFile file,
            @RequestParam("userId") String userId,
            @RequestParam("userName") String userName,
            @RequestParam("roomId") String roomId) {

        try {
            // 1. Upload file lên Cloudinary thông qua FileService
            FileUploaded uploaded = fileService.uploadFile(file, "synkork/voice-notes/" + roomId);
            String fileUrl = uploaded.url(); // Record accessor
            String publicId = uploaded.publicId(); // Record accessor

            // 2. Xử lý chuyển đổi âm thanh (STT) và tóm tắt (Summary) trực tiếp từ MultipartFileư
            String transcript = meetingService.transcribeAudio(file);
            String summaryJson = meetingService.summarizeMeeting(transcript);

            // 3. Trả về kết quả cho frontend
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
