package com.synkork.backend.modules.admin.auditLog.dtos;

import com.synkork.backend.modules.admin.auditLog.enums.LogEntityTypeEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.util.UUID;

@Builder
public record AuditLogRequest(
        @NotBlank(message = "Action không được để trống")
        String action,

        @NotNull(message = "Entity type không được để trống")
        LogEntityTypeEnum entityType,

        @NotBlank(message = "Entity ID không được để trống")
        String entityId,

        String entityName,
        UUID workspaceId,
        String description,
        String metadata
) {}