package com.synkork.backend.common.utils;

import com.synkork.backend.common.dtos.FileUploaded;
import com.synkork.backend.common.dtos.VoiceSummaryResponse;
import com.synkork.backend.common.utils.LLMFunction.MeetingLlmService;
import com.synkork.backend.modules.roomMember.RoomMemberRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/collaboration/voice-summary")
public class VoiceSummaryController {

    private static final Logger log = LoggerFactory.getLogger(VoiceSummaryController.class);

    /** Giới hạn 20 MB */
    private static final long MAX_VOICE_FILE_SIZE = 20 * 1024 * 1024;

    private static final Set<String> ALLOWED_AUDIO_TYPES = Set.of(
            "audio/mpeg", "audio/mp4", "audio/webm", "audio/wav",
            "audio/x-wav", "audio/ogg", "audio/x-m4a"
    );

    private final MeetingLlmService meetingService;
    private final FileService fileService;
    private final RoomMemberRepository roomMemberRepository;

    public VoiceSummaryController(MeetingLlmService meetingService,
                                  FileService fileService,
                                  RoomMemberRepository roomMemberRepository) {
        this.meetingService = meetingService;
        this.fileService = fileService;
        this.roomMemberRepository = roomMemberRepository;
    }

    @PostMapping("/upload")
    public ResponseEntity<?> uploadVoice(
            @RequestParam("file") MultipartFile file,
            @RequestParam("roomId") String roomId) {

        // Validate roomId format
        UUID roomUuid;
        try {
            roomUuid = UUID.fromString(roomId);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("roomId không hợp lệ.");
        }

        // Kiểm tra quyền truy cập room
        UUID currentUserId = AuthUtils.getCurrentUserId();
        if (!roomMemberRepository.existsByRoom_IdAndUser_Id(roomUuid, currentUserId)) {
            return ResponseEntity.status(403).body("Bạn không phải thành viên của phòng này.");
        }

        // Kiểm tra LLM service sẵn sàng
        if (!meetingService.isConfigured()) {
            return ResponseEntity.status(503).body("Dịch vụ AI tạm thời không khả dụng.");
        }

        // Validate file
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("File rỗng.");
        }
        if (file.getSize() > MAX_VOICE_FILE_SIZE) {
            System.out.println("File quá lớn");
            return ResponseEntity.badRequest()
                    .body("File vượt quá giới hạn " + (MAX_VOICE_FILE_SIZE / (1024 * 1024)) + "MB.");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_AUDIO_TYPES.contains(contentType)) {
            System.out.println("Phải đúng chuẩn hỗ trợ");
            return ResponseEntity.badRequest()
                    .body("Loại file không được hỗ trợ. Chỉ chấp nhận: mp3, m4a, webm, wav, ogg.");
        }

        FileUploaded uploaded = null;
        try {
            // 1) Lưu file upload trước để frontend nhận được URL ổn định.
            uploaded = fileService.handleUpload(file, "synkork/voice-notes/" + roomId, false);
            String fileUrl = uploaded.url();
            String publicId = uploaded.publicId();

            // 2) Chuyển audio thành transcript, rồi tóm tắt transcript.
            String transcript = meetingService.transcribeAudio(file);
            String summaryJson = transcript == null || transcript.isBlank()
                    || transcript.trim().equalsIgnoreCase("[không có lời nói]")
                    ? "{}"
                    : meetingService.summarizeMeeting(transcript);

            // 3) Trả về đầy đủ payload dùng DTO sạch sẽ.
            VoiceSummaryResponse response = new VoiceSummaryResponse(
                    "Xử lý voice thành công",
                    fileUrl,
                    publicId,
                    transcript,
                    summaryJson
            );

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            // Dọn dẹp file đã upload nếu xử lý thất bại
            if (uploaded != null) {
                try {
                    fileService.deleteFile(uploaded.publicId(), uploaded.resourceType());
                } catch (Exception cleanupEx) {
                    System.out.println("Không thể dọn dẹp file orphaned publicId"+ uploaded.publicId()+ cleanupEx);
                }
            }
            System.out.println("Lỗi xử lý file voice cho room"+roomId+e );
            return ResponseEntity.status(500).body("Lỗi xử lý file voice: " + e.getMessage());
        }
    }
}
