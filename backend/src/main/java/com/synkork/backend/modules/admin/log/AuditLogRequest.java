package com.synkork.backend.modules.admin.log;

import lombok.Builder;

@Builder
public record AuditLogRequest(
        String action,
        String entityType,
        String entityId,
        String entityName,
        Long workspaceId,
        String description
) {}