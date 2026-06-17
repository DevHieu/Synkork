package com.synkork.backend.modules.admin.auditLog.dtos;

import com.synkork.backend.modules.admin.auditLog.AuditLogEntity;
import com.synkork.backend.modules.admin.auditLog.enums.LogActionEnum;
import com.synkork.backend.modules.admin.auditLog.enums.LogEntityTypeEnum;

import java.time.LocalDateTime;
import java.util.UUID;

public record AuditLogResponse(UUID id,
                               String actorEmail,
                               LogActionEnum action,
                               LogEntityTypeEnum entityType,
                               String entityName,
                               LocalDateTime createdAt) {

    public AuditLogResponse(AuditLogEntity entity) {
        this(
                entity.getId(),
                entity.getActorEmail(),
                entity.getAction(),
                entity.getEntityType(),
                entity.getEntityName(),
                entity.getCreatedAt()
        );
    }
}
