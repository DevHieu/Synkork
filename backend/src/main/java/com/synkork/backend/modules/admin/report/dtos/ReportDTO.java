package com.synkork.backend.modules.admin.report.dtos;

import java.time.LocalDateTime;
import java.util.UUID;

import com.synkork.backend.modules.report.ReportEntity;
import com.synkork.backend.modules.report.enums.ReportStatusEnums;
import com.synkork.backend.modules.report.enums.ReportTypeEnums;

import lombok.Data;

public record ReportDTO(
        UUID id,
        UUID reporterId,
        String reporterEmail,
        UUID targetUserId,
        UUID targetRoomId,
        String targetName,
        String reason,
        String description,
        ReportTypeEnums reportType,
        ReportStatusEnums status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public ReportDTO(ReportEntity e) {
        this(
                e.getId(),
                e.getReporter().getId(),
                e.getReporter().getEmail(),
                e.getTargetUser() != null ? e.getTargetUser().getId() : null,
                e.getTargetRoom() != null ? e.getTargetRoom().getId() : null,
                e.getTargetUser() != null ? e.getTargetUser().getUsername() : e.getTargetRoom().getName(),
                e.getReason(),
                e.getDescription(),
                e.getReportType(),
                e.getStatus(),
                e.getCreatedAt(),
                e.getUpdatedAt()
        );
    }
}
