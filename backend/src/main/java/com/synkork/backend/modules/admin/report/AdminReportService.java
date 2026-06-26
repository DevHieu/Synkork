package com.synkork.backend.modules.admin.report;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.synkork.backend.common.utils.AuthUtils;
import com.synkork.backend.common.utils.EmailService;
import com.synkork.backend.modules.admin.auditLog.AuditLogService;
import com.synkork.backend.modules.admin.auditLog.dtos.BuildLog;
import com.synkork.backend.modules.admin.auditLog.enums.LogActionEnum;
import com.synkork.backend.modules.admin.auditLog.enums.LogEntityTypeEnum;
import com.synkork.backend.modules.admin.report.dtos.ReportDTO;
import com.synkork.backend.modules.admin.report.dtos.ReportFilterRequest;
import com.synkork.backend.modules.admin.report.dtos.ReportUpdateStatusRequest;
import com.synkork.backend.modules.report.ReportEntity;
import com.synkork.backend.modules.report.enums.ReportStatusEnums;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

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

    public List<ReportDTO> getAllReports() {
        return adminReportRepository.findAll()
                .stream()
                .map(ReportDTO::new)
                .toList();
    }

    public ReportDTO getReportById(UUID id) {
        ReportEntity entity = adminReportRepository.findById(id).orElseThrow(() -> new RuntimeException("Report không tồn tại"));

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