package com.synkork.backend.modules.admin.auditLog;

import com.synkork.backend.common.utils.uuid.UuidV7Annotation;
import com.synkork.backend.modules.admin.auditLog.dtos.AuditLogRequest;
import com.synkork.backend.modules.admin.auditLog.enums.LogEntityTypeEnum;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "audit_logs")
@Getter
@Setter
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class AuditLogEntity {

    @Id
    @UuidV7Annotation
    private UUID id;

    private UUID actorId;
    private String actorEmail;

    @Column(nullable = false, length = 100)
    private String action;

    @Enumerated(EnumType.STRING)
    private LogEntityTypeEnum entityType;

    private String entityId;
    private String entityName;
    private UUID workspaceId;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "JSON")
    private String metadata;

    @CreationTimestamp
    private LocalDateTime createdAt;

    public AuditLogEntity(AuditLogRequest request) {
        this.action = request.action();
        this.entityType = request.entityType();
        this.entityId = request.entityId();
        this.entityName = request.entityName();
        this.workspaceId = request.workspaceId();
        this.description = request.description();

    }
}