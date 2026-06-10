package com.synkork.backend.modules.admin.report.dtos;

import com.synkork.backend.modules.report.ReportEntity;
import com.synkork.backend.modules.report.enums.ReportStatusEnums;
import com.synkork.backend.modules.report.enums.ReportTypeEnums;

import java.time.LocalDateTime;
import java.util.UUID;

public record ReportResponse(
        UUID id,
        String reporterEmail,
        String targetName,
        String reason,
        ReportTypeEnums reportType,
        ReportStatusEnums status,
        LocalDateTime createdAt
) {
    public ReportResponse(ReportEntity e) {
        this(
                e.getId(),
                e.getReporter().getEmail(),
                e.getTargetUser() != null ? e.getTargetUser().getUsername() : e.getTargetRoom().getName(),
                e.getReason(),
                e.getReportType(),
                e.getStatus(),
                e.getCreatedAt()
        );
    }
}