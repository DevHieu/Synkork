package com.synkork.backend.modules.admin.report;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.synkork.backend.common.utils.AuthUtils;
import com.synkork.backend.common.utils.EmailService;
import com.synkork.backend.modules.admin.auditLog.AuditLogService;
import com.synkork.backend.modules.admin.auditLog.dtos.BuildLog;
import com.synkork.backend.modules.admin.auditLog.enums.LogActionEnum;
import com.synkork.backend.modules.admin.auditLog.enums.LogEntityTypeEnum;
import com.synkork.backend.modules.admin.report.dtos.ReportFilterRequest;
import com.synkork.backend.modules.admin.report.dtos.ReportResponse;
import com.synkork.backend.modules.admin.report.dtos.ReportUpdateStatusRequest;
import com.synkork.backend.modules.report.ReportEntity;
import com.synkork.backend.modules.report.enums.ReportStatusEnums;

@Service
public class AdminReportService {
    @Autowired
    private AdminReportRepository adminReportRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private AuditLogService auditLogService;

    @Autowired
    private ObjectMapper objectMapper;

    public List<ReportResponse> getAllReports() {
        return adminReportRepository.findAll()
                .stream()
                .map(ReportResponse::new)
                .toList();
    }

    public ReportResponse getReportById(UUID id) {
        ReportEntity entity = adminReportRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Report không tồn tại"));

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

        ReportStatusEnums newStatus = request.status();

        if (report.getStatus() == ReportStatusEnums.RESOLVED
                || report.getStatus() == ReportStatusEnums.DISMISSED) {
            throw new RuntimeException("Report này đã được xử lý xong, không thể thay đổi trạng thái");
        }

        ReportStatusEnums previousStatus = report.getStatus();
        report.setStatus(newStatus);
        ReportEntity savedReport = adminReportRepository.save(report);

        if (newStatus == ReportStatusEnums.RESOLVED || newStatus == ReportStatusEnums.DISMISSED) {
            emailService.sendReportResolvedEmail(
                    savedReport.getReporter().getEmail(),
                    savedReport.getReporter().getDisplayName(),
                    request.note(),
                    newStatus
            );

            BuildLog log = BuildLog.builder()
                    .action(newStatus == ReportStatusEnums.RESOLVED
                            ? LogActionEnum.RESOLVE_REPORT
                            : LogActionEnum.DISMISS_REPORT)
                    .entityType(LogEntityTypeEnum.REPORT)
                    .entityId(savedReport.getId().toString())
                    .entityName(getReportTargetName(savedReport))
                    .workspaceId(savedReport.getTargetRoom() != null ? savedReport.getTargetRoom().getId() : null)
                    .description(AuthUtils.getCurrentUsername() + " processed report " + savedReport.getId())
                    .metadata(createMetadata(previousStatus, savedReport, request.note()))
                    .build();

            auditLogService.log(log);
        }

        return savedReport;
    }

    public void deleteReport(UUID reportId) {
        ReportEntity report = adminReportRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException("Report không tồn tại: " + reportId));
        adminReportRepository.delete(report);

        BuildLog log = BuildLog.builder()
                .action(LogActionEnum.REPORT_DELETED)
                .entityType(LogEntityTypeEnum.REPORT)
                .entityId(report.getId().toString())
                .entityName(getReportTargetName(report))
                .workspaceId(report.getTargetRoom() != null ? report.getTargetRoom().getId() : null)
                .description(AuthUtils.getCurrentUsername() + " deleted report " + report.getId())
                .metadata(createMetadata(report.getStatus(), report, null))
                .build();

        auditLogService.log(log);
    }

    public String createMetadata(ReportStatusEnums previousStatus, ReportEntity report, String note) {
        try {
            Map<String, Object> metadataMap = Map.of(
                    "reportId", report.getId().toString(),
                    "reportType", report.getReportType(),
                    "targetId", getReportTargetId(report),
                    "targetName", getReportTargetName(report),
                    "previousStatus", previousStatus,
                    "newStatus", report.getStatus(),
                    "note", note != null ? note : ""
            );
            return objectMapper.writeValueAsString(metadataMap);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize metadata", e);
        }
    }

    private String getReportTargetId(ReportEntity report) {
        if (report.getTargetUser() != null) {
            return report.getTargetUser().getId().toString();
        }
        if (report.getTargetRoom() != null) {
            return report.getTargetRoom().getId().toString();
        }
        return "";
    }

    private String getReportTargetName(ReportEntity report) {
        if (report.getTargetUser() != null) {
            return report.getTargetUser().getEmail();
        }
        if (report.getTargetRoom() != null) {
            return report.getTargetRoom().getName();
        }
        return "";
    }
}
