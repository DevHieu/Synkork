package com.synkork.backend.modules.admin.report.dtos;

import com.synkork.backend.modules.report.ReportEntity;
import com.synkork.backend.modules.report.enums.ReportReasonEnums;
import com.synkork.backend.modules.report.enums.ReportSeverityEnums;
import com.synkork.backend.modules.report.enums.ReportStatusEnums;
import com.synkork.backend.modules.report.enums.ReportTypeEnums;

import java.time.LocalDateTime;
import java.util.UUID;

public record ReportResponse(
        UUID id,
        UUID reporterId,
        String reporterName,
        String reporterEmail,
        UUID targetUserId,
        UUID targetRoomId,
        String targetName,
        String targetEmail,
        ReportReasonEnums reason,
        String description,
        ReportTypeEnums reportType,
        ReportStatusEnums status,
        ReportSeverityEnums severity,
        LocalDateTime createdAt,
        String evidenceUrl,
        String evidenceName,
        String evidenceResourceType
        ) {
    public ReportResponse(ReportEntity e) {
        this(
                e.getId(),
                e.getReporter().getId(),
                e.getReporter().getDisplayName() != null && !e.getReporter().getDisplayName().isBlank()
                        ? e.getReporter().getDisplayName()
                        : e.getReporter().getUsername(),
                e.getReporter().getEmail(),
                e.getTargetUser() != null ? e.getTargetUser().getId() : null,
                e.getTargetRoom() != null ? e.getTargetRoom().getId() : null,
                e.getTargetUser() != null ? e.getTargetUser().getUsername()
                        : e.getTargetRoom().getName(),
                e.getTargetUser() != null
                        ? e.getTargetUser().getEmail()
                        : e.getTargetRoom().getOwner() != null
                        ? e.getTargetRoom().getOwner().getEmail()
                        : null,
                e.getReason(),
                e.getDescription(),
                e.getReportType(),
                e.getStatus(),
                e.getSeverity(),
                e.getCreatedAt(),
                e.getEvidenceUrl(),
                e.getEvidenceName(),
                e.getEvidenceResourceType());
    }
}