package com.synkork.backend.modules.admin.auditLog;

import com.synkork.backend.modules.admin.auditLog.dtos.AuditLogFilterRequest;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class AuditLogSpecification {

    public static Specification<AuditLogEntity> filter(
            AuditLogFilterRequest request
    ) {
        return (root, query, cb) -> {

            List<Predicate> predicates = new ArrayList<>();

            if (hasText(request.search())) {
                String keyword =
                        "%" + request.search().trim().toLowerCase() + "%";

                Predicate searchEmail = cb.like(
                        cb.lower(root.get("actorEmail")),
                        keyword
                );

                Predicate searchDesc = cb.like(
                        cb.lower(root.get("description")),
                        keyword
                );

                predicates.add(
                        cb.or(searchEmail, searchDesc)
                );
            }

            if (hasText(request.action())) {
                String actionKeyword = "%" + request.action().trim().toLowerCase() + "%";
                predicates.add(
                        cb.like(
                                cb.upper(root.get("action")), actionKeyword
                        )
                );
            }

            if (request.entityType() != null) {
                predicates.add(
                        cb.equal(
                                root.get("entityType"),
                                request.entityType()
                        )
                );
            }

            if (request.workspaceId() != null) {
                predicates.add(
                        cb.equal(
                                root.get("workspaceId"),
                                request.workspaceId()
                        )
                );
            }

            if (hasText(request.actorEmail())) {
                predicates.add(
                        cb.equal(
                                root.get("actorEmail"),
                                request.actorEmail().trim()
                        )
                );
            }

            if (request.dateFrom() != null) {
                predicates.add(
                        cb.greaterThanOrEqualTo(
                                root.get("createdAt"),
                                request.dateFrom()
                        )
                );
            }

            if (request.dateTo() != null) {
                predicates.add(
                        cb.lessThanOrEqualTo(
                                root.get("createdAt"),
                                request.dateTo()
                        )
                );
            }

            return cb.and(
                    predicates.toArray(new Predicate[0])
            );
        };
    }

    private static boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
