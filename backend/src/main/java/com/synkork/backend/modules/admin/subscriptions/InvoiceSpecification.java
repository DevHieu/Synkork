package com.synkork.backend.modules.admin.subscriptions;

import com.synkork.backend.modules.admin.subscriptions.dtos.InvoiceFilterRequest;
import com.synkork.backend.modules.payment.InvoiceEntity;
import com.synkork.backend.modules.user.UserEntity;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class InvoiceSpecification {

    public static Specification<InvoiceEntity> filter(InvoiceFilterRequest filter) {
        return (root, query, cb) -> {

            if (Long.class != query.getResultType() && long.class != query.getResultType()) {
                root.fetch("user", JoinType.LEFT);
            }

            List<Predicate> predicates = new ArrayList<>();
            Join<InvoiceEntity, UserEntity> userJoin = null;

            if (hasText(filter.search()) || filter.plan() != null) {
                userJoin = root.join("user", JoinType.LEFT);
            }

            if (hasText(filter.search())) {
                String keyword = "%" + filter.search().trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(userJoin.get("username")), keyword),
                        cb.like(cb.lower(userJoin.get("email")), keyword)
                ));
            }

            if (filter.status() != null) {
                predicates.add(cb.equal(root.get("status"), filter.status()));
            }

            if (filter.plan() != null) {
                predicates.add(cb.equal(userJoin.get("currentPlan"), filter.plan()));
            }

            if (filter.paymentMethod() != null) {
                predicates.add(cb.equal(root.get("paymentMethod"), filter.paymentMethod()));
            }

            if (filter.dateFrom() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), filter.dateFrom()));
            }

            if (filter.dateTo() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), filter.dateTo()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private static boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
