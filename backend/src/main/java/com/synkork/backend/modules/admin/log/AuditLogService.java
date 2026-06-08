package com.synkork.backend.modules.admin.log;

import com.synkork.backend.common.utils.AuthUtils;
import com.synkork.backend.modules.admin.log.dtos.AuditLogRequest;
import com.synkork.backend.modules.admin.log.dtos.BuildLog;
import jakarta.persistence.criteria.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class AuditLogService {

    @Autowired
    private AuditLogRepository auditLogRepository;

    public Page<AuditLogEntity> findAll(
            String search,
            String action,
            String entityType,
            Long workspaceId,
            String actorEmail,
            LocalDateTime fromDate,
            LocalDateTime toDate,
            Pageable pageable) {
        System.out.println("SEARCH: " + search);
        System.out.println("ACTION: " + action);
        Specification<AuditLogEntity> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 1. Search global (Tìm theo Email HOẶC Description)
            if (search != null && !search.trim().isEmpty()) {
                String keyword = "%" + search.trim().toLowerCase() + "%";
                Predicate searchEmail = cb.like(cb.lower(root.get("actorEmail")), keyword);
                Predicate searchDesc = cb.like(cb.lower(root.get("description")), keyword);
                predicates.add(cb.or(searchEmail, searchDesc));
            }

            // 2. Filter theo Action (Chính xác)
            if (action != null && !action.trim().isEmpty()) {
                String  keyword = "%" + action.trim().toUpperCase() + "%";
                predicates.add(cb.like(cb.upper(root.get("action")), keyword));
            }

            // 3. Filter theo Entity Type (Chính xác)
            if (entityType != null && !entityType.trim().isEmpty()) {
                predicates.add(cb.equal(root.get("entityType"), entityType));
            }

            // 5. Filter theo Workspace ID
            if (workspaceId != null) {
                predicates.add(cb.equal(root.get("workspaceId"), workspaceId));
            }

            // 6. Filter theo Actor Email (Chính xác)
            if (actorEmail != null && !actorEmail.trim().isEmpty()) {
                predicates.add(cb.equal(root.get("actorEmail"), actorEmail));
            }

            // 7. Filter theo khoảng thời gian (Từ ngày - Đến ngày)
            if (fromDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), fromDate));
            }
            if (toDate != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), toDate));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

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
        AuditLogEntity entity = new AuditLogEntity(request);
        return auditLogRepository.save(entity);
    }
}
