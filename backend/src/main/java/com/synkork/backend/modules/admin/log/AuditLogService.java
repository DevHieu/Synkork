package com.synkork.backend.modules.admin.log;

import com.synkork.backend.common.utils.AuthUtils;
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
            String workspaceId,
            String actorEmail,
            LocalDateTime fromDate,
            LocalDateTime toDate,
            Pageable pageable) {

        Specification<AuditLogEntity> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (search != null && !search.trim().isEmpty()) {
                String keyword = "%" + search.trim().toLowerCase() + "%";
                Predicate searchEmail = cb.like(cb.lower(root.get("actorEmail")), keyword);
                Predicate searchDesc = cb.like(cb.lower(root.get("description")), keyword);
                predicates.add(cb.or(searchEmail, searchDesc));
            }

            if (action != null && !action.trim().isEmpty()) {
                predicates.add(cb.equal(root.get("action"), action));
            }

            if (entityType != null && !entityType.trim().isEmpty()) {
                predicates.add(cb.equal(root.get("entityType"), entityType));
            }

            if (workspaceId != null) {
                predicates.add(cb.equal(root.get("workspaceId"), workspaceId));
            }

            if (actorEmail != null && !actorEmail.trim().isEmpty()) {
                predicates.add(cb.equal(root.get("actorEmail"), actorEmail));
            }

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

    public void log(String action, String entityType, String entityId,
                    String entityName, UUID workspaceId, String description) {

        // Lấy actor từ SecurityContext
        String email = AuthUtils.getCurrentUsername();

        AuditLogEntity log = AuditLogEntity.builder()
                .actorEmail(email)
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .entityName(entityName)
                .workspaceId(workspaceId)
                .description(description)
                .build();

        auditLogRepository.save(log);
    }

    public AuditLogEntity findById(String id) {
        return auditLogRepository.findById(UUID.fromString(id)).orElseThrow(() -> new RuntimeException("Log không tồn tại"));
    }
}
