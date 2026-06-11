package com.synkork.backend.modules.admin.auditLog.dtos;

import com.synkork.backend.modules.admin.auditLog.AuditLogEntity;
import com.synkork.backend.modules.admin.auditLog.enums.LogEntityTypeEnum;

import java.time.LocalDateTime;
import java.util.UUID;

public record AuditLogDetailResponse(
        UUID id,
        UUID actorId,
        String actorEmail,
        String action,
        LogEntityTypeEnum entityType,
        String entityId,
        String entityName,
        UUID workspaceId,
        String description,
        String metadata,
        LocalDateTime createdAt
) {
    public AuditLogDetailResponse(AuditLogEntity entity) {
        this(
                entity.getId(),
                entity.getActorId(),
                entity.getActorEmail(),
                entity.getAction(),
                entity.getEntityType(),
                entity.getEntityId(),
                entity.getEntityName(),
                entity.getWorkspaceId(),
                entity.getDescription(),
                entity.getMetadata(),
                entity.getCreatedAt()
        );
    }
}