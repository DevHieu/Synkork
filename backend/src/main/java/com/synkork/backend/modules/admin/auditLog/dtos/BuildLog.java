package com.synkork.backend.modules.admin.auditLog.dtos;

import com.synkork.backend.modules.admin.auditLog.enums.LogEntityTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BuildLog {
    private String action;
    private LogEntityTypeEnum entityType;
    private String entityId;
    private String entityName;
    private String workspaceId;
    private String description;
    private String metadata;
}
