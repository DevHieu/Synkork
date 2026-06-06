package com.synkork.backend.modules.admin.log;

import com.synkork.backend.common.utils.uuid.UuidV7Annotation;
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

    @Column(length = 50)
    private String entityType;

    private String entityId;
    private String entityName;
    private UUID workspaceId;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "JSON")
    private String metadata;

    @CreationTimestamp
    private LocalDateTime createdAt;
}