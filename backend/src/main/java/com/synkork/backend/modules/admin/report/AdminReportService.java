package com.synkork.backend.modules.admin.report;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.synkork.backend.common.utils.AuthUtils;
import com.synkork.backend.common.utils.FileService;
import com.synkork.backend.modules.admin.auditLog.AuditLogService;
import com.synkork.backend.modules.admin.auditLog.dtos.BuildLog;
import com.synkork.backend.modules.admin.auditLog.enums.LogActionEnum;
import com.synkork.backend.modules.admin.auditLog.enums.LogEntityTypeEnum;
import com.synkork.backend.modules.admin.report.dtos.ReportFilterRequest;
import com.synkork.backend.modules.admin.report.dtos.ReportResponse;
import com.synkork.backend.modules.admin.report.dtos.ReportUpdateStatusRequest;
import com.synkork.backend.modules.admin.report.email.AdminReportEmailService;
import com.synkork.backend.modules.admin.statistics.dtos.ReportChartResponse;
import com.synkork.backend.modules.admin.statistics.dtos.ReportReasonStatsResponse;
import com.synkork.backend.modules.admin.statistics.dtos.ReportStatsResponse;
import com.synkork.backend.modules.report.ReportEntity;
import com.synkork.backend.modules.report.enums.ReportStatusEnums;
import com.synkork.backend.modules.report.enums.ReportTypeEnums;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class AdminReportService {
    @Autowired
    private AdminReportRepository adminReportRepository;

    @Autowired
    private AdminReportEmailService adminReportEmailService;

    @Autowired
    private AuditLogService auditLogService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired 
    private FileService fileService;

    public ReportStatsResponse getReportStatsData(LocalDateTime dateFrom, LocalDateTime dateTo) {
        boolean hasRange = dateFrom != null && dateTo != null;

        long total = hasRange
                ? adminReportRepository.countByCreatedAtBetween(dateFrom, dateTo)
                : adminReportRepository.count();
        long pending = hasRange
                ? adminReportRepository.countByStatusAndCreatedAtBetween(ReportStatusEnums.PENDING, dateFrom, dateTo)
                : adminReportRepository.countByStatus(ReportStatusEnums.PENDING);
        long resolved = hasRange
                ? adminReportRepository.countByStatusAndCreatedAtBetween(ReportStatusEnums.RESOLVED, dateFrom, dateTo)
                : adminReportRepository.countByStatus(ReportStatusEnums.RESOLVED);
        long dismissed = hasRange
                ? adminReportRepository.countByStatusAndCreatedAtBetween(ReportStatusEnums.DISMISSED, dateFrom, dateTo)
                : adminReportRepository.countByStatus(ReportStatusEnums.DISMISSED);
        long userReports = hasRange
                ? adminReportRepository.countByReportTypeAndCreatedAtBetween(ReportTypeEnums.USER, dateFrom, dateTo)
                : adminReportRepository.countByReportType(ReportTypeEnums.USER);
        long roomReports = hasRange
                ? adminReportRepository.countByReportTypeAndCreatedAtBetween(ReportTypeEnums.ROOM, dateFrom, dateTo)
                : adminReportRepository.countByReportType(ReportTypeEnums.ROOM);

        return new ReportStatsResponse(total, pending, resolved, dismissed, userReports, roomReports);
    }

    public List<ReportChartResponse> getReportChart(LocalDateTime dateFrom, LocalDateTime dateTo) {
        return adminReportRepository.findDailyReportCounts(dateFrom, dateTo)
                .stream()
                .map(row -> new ReportChartResponse(
                        (LocalDate) row[0],
                        ((Number) row[1]).longValue(),
                        ((Number) row[2]).longValue()))
                .toList();
    }

    public List<ReportReasonStatsResponse> getReportReasonStats(LocalDateTime dateFrom, LocalDateTime dateTo) {
        return adminReportRepository.findReasonCountsGroupedByType(dateFrom, dateTo);
    }

    public ReportResponse getReportById(UUID id) {
        ReportEntity entity = adminReportRepository.findById(id).orElseThrow(() -> new RuntimeException("Report không tồn tại"));

        return new ReportResponse(entity);
    }

    public Page<ReportEntity> getFilteredReports(ReportFilterRequest request) {

        request.validate(); // validate dateFrom and dateTo

        Specification<ReportEntity> spec = ReportSpecification.from(request);
 
        Pageable pageable = PageRequest.of(
                request.getPage(),
                request.getSize(),
                Sort.by(Sort.Direction.DESC, "createdAt")
        );
 
        return adminReportRepository.findAll(spec, pageable);
    }
 
    public ReportEntity updateReportStatus(UUID reportId, ReportUpdateStatusRequest request) {
        ReportEntity report = adminReportRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException("Report không tồn tại: " + reportId));

        if (report.getStatus() == ReportStatusEnums.RESOLVED
                || report.getStatus() == ReportStatusEnums.DISMISSED) {
            throw new RuntimeException("Report này đã được xử lý xong, không thể thay đổi trạng thái");
        }

        ReportStatusEnums newStatus = request.status();

        if(Boolean.TRUE.equals(request.hasWarn())) {
                report.setHasWarn(true);
        }

        if (newStatus == ReportStatusEnums.DISMISSED && (Boolean.TRUE.equals(report.getHasWarn()) || Boolean.TRUE.equals(request.hasWarn()))) {
                throw new RuntimeException("Đã cảnh cáo đối tượng, không thể bác bỏ báo cáo này");
        }

        report.setStatus(newStatus);
        
        ReportEntity savedReport = adminReportRepository.save(report);

        if (newStatus == ReportStatusEnums.RESOLVED || newStatus == ReportStatusEnums.DISMISSED) {
            adminReportEmailService.sendReportResolvedEmail(
                    savedReport.getReporter().getEmail(),
                    savedReport.getReporter().getDisplayName(),
                    request.note(),
                    newStatus
            );

            BuildLog log = BuildLog.builder()
                    .action(newStatus == ReportStatusEnums.RESOLVED ? LogActionEnum.RESOLVE_REPORT : LogActionEnum.DISMISS_REPORT)
                    .entityType(LogEntityTypeEnum.REPORT)
                    .entityId(savedReport.getId().toString())
                    .description(AuthUtils.getCurrentUsername() + " Đã xử lí tố cáo của user " + savedReport.getTargetUser().getEmail())
                    .metadata(this.createMetadata(report, savedReport))
                    .build();

            auditLogService.log(log);
        }

        return savedReport;
    }

    public void deleteReport(UUID reportId) {
        ReportEntity report = adminReportRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException("Report không tồn tại: " + reportId));
        adminReportRepository.delete(report);

        if(report.getEvidenceUrl() != null){
            fileService.deleteFile(report.getEvidencePublicId(), report.getEvidenceResourceType());
        }

        BuildLog log = BuildLog.builder()
                .action(LogActionEnum.REPORT_DELETED)
                .entityType(LogEntityTypeEnum.REPORT)
                .entityId(report.getId().toString())
                .description(AuthUtils.getCurrentUsername() + " Đã xóa tố cáo")
                .metadata(this.createMetadata(report, report))
                .build();

        auditLogService.log(log);
    }

    public String createMetadata(ReportEntity previousReport, ReportEntity newReport) {
        try {
            Map<String, Object> metadataMap = Map.of(
                    "reportId", newReport.getId().toString(),
                    "reportType", newReport.getReportType(),
                    "targetUserId", newReport.getTargetUser().getId().toString(),
                    "targetUserEmail", newReport.getTargetUser().getEmail(),
                    "previousStatus", previousReport.getStatus()
            );
            return objectMapper.writeValueAsString(metadataMap);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize metadata", e);
        }
    }
}
