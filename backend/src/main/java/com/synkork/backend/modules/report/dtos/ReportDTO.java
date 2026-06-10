package com.synkork.backend.modules.report.dtos;

import java.time.LocalDateTime;
import java.util.UUID;

import com.synkork.backend.modules.report.ReportEntity;
import com.synkork.backend.modules.report.enums.ReportStatusEnums;
import com.synkork.backend.modules.report.enums.ReportTypeEnums;

import lombok.Data;

@Data
public class ReportDTO {
    private UUID id;
    private UUID reporterId;
    private UUID targetUserId;
    private UUID targetRoomId;
    private String targetName;
    private String reason;
    private String description;
    private ReportTypeEnums reportType;
    private ReportStatusEnums status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public ReportDTO(ReportEntity e) {
        this.id = e.getId();
        this.reporterId = e.getReporterId();
        this.reason = e.getReason();
        this.description = e.getDescription();
        this.reportType = e.getReportType();
        this.status = e.getStatus();
        this.createdAt = e.getCreatedAt();
        this.updatedAt = e.getUpdatedAt();

        if (e.getTargetUser() != null) {
            this.targetUserId = e.getTargetUser().getId();
            this.targetName = e.getTargetUser().getUsername();
        }

        if (e.getTargetRoom() != null) {
            this.targetRoomId = e.getTargetRoom().getId();
            this.targetName = e.getTargetRoom().getName();
        }
    }
}
