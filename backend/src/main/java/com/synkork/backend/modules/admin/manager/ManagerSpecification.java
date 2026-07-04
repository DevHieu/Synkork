package com.synkork.backend.modules.admin.manager;

import com.synkork.backend.modules.admin.manager.dto.ManagerFilterRequest;
import com.synkork.backend.modules.user.UserEntity;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class ManagerSpecification {

    public static Specification<UserEntity> filter(ManagerFilterRequest request) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (hasText(request.search())) {
                String keyword = "%" + request.search().trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("username")), keyword),
                        cb.like(cb.lower(root.get("email")), keyword)
                ));
            }

            if (request.role() != null) {
                predicates.add(cb.equal(root.get("role"), request.role()));
            }

            if (request.status() != null) {
                predicates.add(cb.equal(root.get("status"), request.status()));
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

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private static boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
