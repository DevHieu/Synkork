package com.synkork.backend.modules.report;

import com.synkork.backend.common.utils.AuthUtils;
import com.synkork.backend.modules.admin.report.ReportSpecification;
import com.synkork.backend.modules.admin.report.dtos.ReportDTO;
import com.synkork.backend.modules.admin.report.dtos.ReportFilterRequest;
import com.synkork.backend.modules.admin.report.dtos.ReportUpdateStatusRequest;
import com.synkork.backend.modules.report.dtos.*;
import com.synkork.backend.modules.report.enums.ReportStatusEnums;
import com.synkork.backend.modules.report.enums.ReportTypeEnums;
import com.synkork.backend.modules.room.RoomEntity;
import com.synkork.backend.modules.room.RoomRepository;
import com.synkork.backend.modules.user.UserEntity;
import com.synkork.backend.modules.user.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
public class ReportService {

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private UserService userService;

    public ReportEntity createReport(ReportRequestDto request, ReportTypeEnums type) {
        UUID reporterId = AuthUtils.getCurrentUserId();
        UUID targetId = UUID.fromString(request.targetId());

        // Tránh tự report bản thân
        if (type == ReportTypeEnums.USER && reporterId.equals(targetId)) {
            throw new RuntimeException("Không thể tự báo cáo bản thân");
        }

        UserEntity reporter = userService.findById(reporterId);

        ReportEntity.ReportEntityBuilder builder = ReportEntity.builder()
                .reason(request.reason())
                .description(request.description())
                .reporter(reporter)
                .reportType(type)
                .status(ReportStatusEnums.PENDING);

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

    public List<ReportDTO> getAllReports() {
        return reportRepository.findAll()
                .stream()
                .map(ReportDTO::new)
                .toList();
    }

    public ReportDTO getReportById(UUID id) {
        ReportEntity entity = reportRepository.findById(id).orElseThrow(() -> new RuntimeException("Report không tồn tại"));

        return new ReportDTO(entity);
    }

    public Page<ReportEntity> getFilteredReports(ReportFilterRequest request) {

        request.validate(); // validate dateFrom and dateTo

        Specification<ReportEntity> spec = ReportSpecification.from(request);
 
        Pageable pageable = PageRequest.of(
                request.getPage(),
                request.getSize(),
                Sort.by(Sort.Direction.DESC, "createdAt")
        );
 
        return reportRepository.findAll(spec, pageable);
    }
 
    public ReportEntity updateReportStatus(UUID reportId, ReportUpdateStatusRequest request) {
        ReportEntity report = reportRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException("Report không tồn tại: " + reportId));
 
        ReportStatusEnums newStatus = request.status();
 
        if (report.getStatus() == ReportStatusEnums.RESOLVED
                || report.getStatus() == ReportStatusEnums.DISMISSED) {
            throw new RuntimeException("Report này đã được xử lý xong, không thể thay đổi trạng thái");
        }
 
        report.setStatus(newStatus);
        return reportRepository.save(report);
    }

    public void deleteReport(UUID reportId) {
        ReportEntity report = reportRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException("Report không tồn tại: " + reportId));
        reportRepository.delete(report);
    }
}
