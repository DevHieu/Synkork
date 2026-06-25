package com.synkork.backend.modules.admin.auditLog.dtos;

import com.synkork.backend.modules.admin.auditLog.enums.LogActionEnum;
import com.synkork.backend.modules.admin.auditLog.enums.LogEntityTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BuildLog {
    private LogActionEnum action;
    private LogEntityTypeEnum entityType;
    private String entityId;
    private String entityName;
    private UUID workspaceId;
    private String description;
    private String metadata;
}
