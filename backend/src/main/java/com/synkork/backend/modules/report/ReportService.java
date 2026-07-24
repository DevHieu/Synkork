package com.synkork.backend.modules.report;

import com.synkork.backend.common.dtos.FileUploaded;
import com.synkork.backend.common.utils.AuthUtils;
import com.synkork.backend.common.utils.FileService;
import com.synkork.backend.modules.report.dtos.*;
import com.synkork.backend.modules.report.enums.ReportStatusEnums;
import com.synkork.backend.modules.report.enums.ReportTypeEnums;
import com.synkork.backend.modules.room.RoomEntity;
import com.synkork.backend.modules.room.RoomRepository;
import com.synkork.backend.modules.user.UserEntity;
import com.synkork.backend.modules.user.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Slf4j
@Service
public class ReportService {

    private static final long MAX_IMAGE_SIZE = 10L * 1024 * 1024;
    private static final long MAX_VIDEO_SIZE = 50L * 1024 * 1024;

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private FileService fileService;

    public ReportEntity createReport(ReportRequestDto request, ReportTypeEnums type, MultipartFile evidence) {
        UUID reporterId = AuthUtils.getCurrentUserId();
        UUID targetId = UUID.fromString(request.targetId());

        // Tránh tự report bản thân
        if (type == ReportTypeEnums.USER && reporterId.equals(targetId)) {
            throw new RuntimeException("Không thể tự báo cáo bản thân");
        }

        UserEntity reporter = userService.findById(reporterId);

        ReportEntity.ReportEntityBuilder builder = ReportEntity.builder()
                .reason(request.reason())
                .severity(request.reason().getDefaultSeverity())
                .description(request.description())
                .reporter(reporter)
                .reportType(type)
                .status(ReportStatusEnums.PENDING);

        attachEvidence(builder, evidence);

        if (type == ReportTypeEnums.USER) {
            UserEntity targetUser = userService.findById(targetId);

            if (reportRepository.existsByReporterIdAndTargetUserAndReportType(reporterId, targetUser, type)) {
                throw new RuntimeException("Bạn đã báo cáo người dùng này rồi");
            }

            builder.targetUser(targetUser);
        } else {
            RoomEntity targetRoom = roomRepository.findById(targetId)
                    .orElseThrow(() -> new RuntimeException("Room không tồn tại"));

            if (reportRepository.existsByReporterIdAndTargetRoomAndReportType(reporterId, targetRoom, type)) {
                throw new RuntimeException("Bạn đã báo cáo phòng này rồi");
            }

            builder.targetRoom(targetRoom);
        }

        return reportRepository.save(builder.build());
    }

    public void attachEvidence(ReportEntity.ReportEntityBuilder builder, MultipartFile evidence){
        if (evidence == null || evidence.isEmpty()) return;

        boolean isImage = evidence.getContentType() != null && evidence.getContentType().startsWith("image/");
        boolean isVideo = evidence.getContentType() != null && evidence.getContentType().startsWith("video/");

        if(!isImage && !isVideo) throw new RuntimeException("Chỉ chấp nhận tệp ảnh hoặc video làm bằng chứng!");

        long maxSize = isVideo ? MAX_VIDEO_SIZE : MAX_IMAGE_SIZE;

        if (evidence.getSize() > maxSize) {
            long maxMB = maxSize / (1024 * 1024);
            throw new RuntimeException("Tệp bằng chứng vượt quá "+ maxMB + "MB cho phép");
        }

        FileUploaded uploaded = fileService.handleUpload(evidence, "report_file", false);
   
        builder.evidenceUrl(uploaded.url())
               .evidencePublicId(uploaded.publicId())
               .evidenceResourceType(uploaded.resourceType())
               .evidenceName(uploaded.originalName());
    }
}
