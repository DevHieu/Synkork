package com.synkork.backend.modules.admin.subscriptions;

import com.synkork.backend.modules.admin.subscriptions.dtos.InvoiceFilterRequest;
import com.synkork.backend.modules.payment.InvoiceEntity;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class InvoiceSpecification {

    public static Specification<InvoiceEntity> filter(InvoiceFilterRequest filter) {
        return (root, query, cb) -> {
            // Fetch user
            if (Long.class != query.getResultType() && long.class != query.getResultType()) {
                root.fetch("user", jakarta.persistence.criteria.JoinType.LEFT);
            }

            List<Predicate> predicates = new ArrayList<>();

            if (hasText(filter.search())) {
                String keyword = "%" + filter.search().trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("user").get("username")), keyword),
                        cb.like(cb.lower(root.get("user").get("email")), keyword)
                ));
            }

            if (filter.status() != null) {
                predicates.add(cb.equal(root.get("status"), filter.status()));
            }

            if (filter.plan() != null) {
                predicates.add(cb.equal(root.get("user").get("currentPlan"), filter.plan()));
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
