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

@Service
public class AuditLogService {

    @Autowired
    private AuditLogRepository auditLogRepository;

    public Page<AuditLogEntity> findAll(
            String search,
            String action,
            String entityType,
            AuditLogEntity.AuditStatus status,
            Long workspaceId,
            String actorEmail,
            LocalDateTime fromDate,
            LocalDateTime toDate,
            Pageable pageable) {

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
                predicates.add(cb.equal(root.get("action"), action));
            }

            // 3. Filter theo Entity Type (Chính xác)
            if (entityType != null && !entityType.trim().isEmpty()) {
                predicates.add(cb.equal(root.get("entityType"), entityType));
            }

            // 4. Filter theo Status (Enum SUCCESS/FAILURE)
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
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

    public void log(String action, String entityType, String entityId,
                    String entityName, Long workspaceId, String description) {

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
                .status(AuditLogEntity.AuditStatus.SUCCESS)
                .build();

        auditLogRepository.save(log);
    }
}
