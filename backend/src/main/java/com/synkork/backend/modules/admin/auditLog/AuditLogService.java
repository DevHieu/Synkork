package com.synkork.backend.modules.admin.auditLog;

import com.synkork.backend.common.utils.AuthUtils;
import com.synkork.backend.modules.admin.auditLog.dtos.AuditLogFilterRequest;
import com.synkork.backend.modules.admin.auditLog.dtos.AuditLogRequest;
import com.synkork.backend.modules.admin.auditLog.dtos.BuildLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AuditLogService {

    @Autowired
    private AuditLogRepository auditLogRepository;

    public Page<AuditLogEntity> findAll(AuditLogFilterRequest request) {

        request.validate(); // validate dateFrom and dateTo

        Pageable pageable = PageRequest.of(
                request.getPage(),
                request.getSize()
        );

        Specification<AuditLogEntity> spec =
                AuditLogSpecification.filter(request);

        return auditLogRepository.findAll(spec, pageable);
    }

    public void log(BuildLog data) {

        // Lấy actor từ SecurityContext
        String email = AuthUtils.getCurrentUsername();

        AuditLogEntity log = AuditLogEntity.builder()
                .actorEmail(email)
                .action(data.getAction())
                .entityType(data.getEntityType())
                .entityId(data.getEntityId())
                .entityName(data.getEntityName())
                .workspaceId(UUID.fromString(data.getWorkspaceId()))
                .description(data.getDescription())
                .metadata(data.getMetadata())
                .build();

        auditLogRepository.save(log);
    }

    public AuditLogEntity findById(String id) {
        return auditLogRepository.findById(UUID.fromString(id)).orElseThrow(() -> new RuntimeException("Log không tồn tại"));
    }

    public AuditLogEntity createLog(AuditLogRequest request) {
        String email = AuthUtils.getCurrentUsername();
        AuditLogEntity entity = new AuditLogEntity(request);
        entity.setActorEmail(email);
        entity.setMetadata(request.metadata());
        return auditLogRepository.save(entity);
    }

    public AuditLogEntity updateLog(String id, AuditLogRequest request) {
        AuditLogEntity entity = findById(id);
        entity.setAction(request.action());
        entity.setEntityType(request.entityType());
        entity.setEntityId(request.entityId());
        entity.setEntityName(request.entityName());
        entity.setWorkspaceId(request.workspaceId());
        entity.setDescription(request.description());
        entity.setMetadata(request.metadata());
        return auditLogRepository.save(entity);
    }

    public void deleteLog(String id) {
        AuditLogEntity entity = findById(id);
        auditLogRepository.delete(entity);
    }
}
