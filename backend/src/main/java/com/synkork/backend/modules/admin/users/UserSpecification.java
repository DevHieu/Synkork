package com.synkork.backend.modules.admin.users;

import com.synkork.backend.modules.admin.users.dtos.UserFilterRequest;
import com.synkork.backend.modules.user.UserEntity;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class UserSpecification {

    public static Specification<UserEntity> filter(UserFilterRequest request) {
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

            if (request.plan() != null) {
                predicates.add(cb.equal(root.get("currentPlan"), request.plan()));
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

            if (request.minWarning() != null) {
                predicates.add(cb.greaterThanOrEqualTo(
                        root.get("warning"),
                        request.minWarning()
                ));
            }

            if (request.maxWarning() != null) {
                predicates.add(cb.lessThanOrEqualTo(
                        root.get("warning"),
                        request.maxWarning()
                ));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private static boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
