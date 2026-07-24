package com.synkork.backend.modules.admin.changePassword;

import com.synkork.backend.modules.admin.changePassword.dto.PasswordResetRequestFilter;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class PasswordResetRequestSpecification {

    public static Specification<PasswordResetRequestEntity> filter(PasswordResetRequestFilter request) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            Join<Object, Object> user = root.join("user");

            if (hasText(request.search())) {
                String keyword = "%" + request.search().trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(user.get("username")), keyword),
                        cb.like(cb.lower(user.get("displayName")), keyword),
                        cb.like(cb.lower(user.get("email")), keyword)
                ));
            }

            if (request.status() != null) {
                predicates.add(cb.equal(root.get("status"), request.status()));
            }

            if (request.dateFrom() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), request.dateFrom()));
            }

            if (request.dateTo() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), request.dateTo()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private static boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
