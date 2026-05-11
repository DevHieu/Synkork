package com.synkork.backend.common.utils;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

@RestController
@RequestMapping("/api/collaboration/voice-summary")
public class llmServiceVoice {

    private final String UPLOAD_DIR = "src/main/resources/uploads/voice/";

    @PostMapping("/upload")
    public ResponseEntity<?> uploadVoice(
            @RequestParam("file") MultipartFile file,
            @RequestParam("userId") String userId,
            @RequestParam("userName") String userName,
            @RequestParam("roomId") String roomId) {

        try {
            // Tạo thư mục trong resources nếu chưa tồn tại
            File dir = new File(UPLOAD_DIR);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            // Đặt tên file kèm thời gian để tránh trùng
            String fileName = String.format("%s_%s_%s.webm", roomId, userId, System.currentTimeMillis());
            File destination = new File(dir.getAbsolutePath() + File.separator + fileName);

            // 3Lưu file
            file.transferTo(destination);

            System.out.println("=== Đã nhận voice thành công ===");
            System.out.println("User: " + userName);
            System.out.println("Room: " + roomId);
            System.out.println("Saved to: " + destination.getAbsolutePath());

            return ResponseEntity.ok("File uploaded successfully to resources: " + fileName);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error uploading file: " + e.getMessage());
        }
    }
}
