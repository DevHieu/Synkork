package com.synkork.backend.modules.admin.subscriptions.specification;

import com.synkork.backend.modules.admin.subscriptions.dtos.AdminSubscriptionFilterRequest;
import com.synkork.backend.modules.payment.entity.InvoiceEntity;
import com.synkork.backend.modules.payment.entity.UserSubscriptionEntity;
import com.synkork.backend.modules.user.UserEntity;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AdminSubscriptionSpecification {

    public static Specification<UserSubscriptionEntity> filter(AdminSubscriptionFilterRequest filter) {
        return (root, query, cb) -> {
            if (Long.class != query.getResultType() && long.class != query.getResultType()) {
                root.fetch("user", JoinType.LEFT);
                root.fetch("invoice", JoinType.LEFT);
            }

            List<Predicate> predicates = new ArrayList<>();
            Join<UserSubscriptionEntity, UserEntity> userJoin = null;
            Join<UserSubscriptionEntity, InvoiceEntity> invoiceJoin = null;

            if (hasText(filter.search())) {
                userJoin = root.join("user", JoinType.LEFT);
                invoiceJoin = root.join("invoice", JoinType.LEFT);

                String rawSearch = filter.search().trim();
                String keyword = "%" + rawSearch.toLowerCase() + "%";

                List<Predicate> searchPredicates = new ArrayList<>();
                searchPredicates.add(cb.like(cb.lower(userJoin.get("username")), keyword));
                searchPredicates.add(cb.like(cb.lower(userJoin.get("email")), keyword));

                try {
                    UUID searchId = UUID.fromString(rawSearch);
                    searchPredicates.add(cb.equal(root.get("id"), searchId));
                    searchPredicates.add(cb.equal(invoiceJoin.get("id"), searchId));
                } catch (IllegalArgumentException ignored) {
                    // Ignore UUID search when the keyword is not a valid UUID.
                }

                predicates.add(cb.or(searchPredicates.toArray(new Predicate[0])));
            }

            if (filter.plan() != null) {
                predicates.add(cb.equal(root.get("plan"), filter.plan()));
            }

            if (filter.status() != null) {
                predicates.add(cb.equal(root.get("status"), filter.status()));
            }

            if (filter.current() != null) {
                predicates.add(cb.equal(root.get("current"), filter.current()));
            }

            if (filter.expiresFrom() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("expiresAt"), filter.expiresFrom()));
            }

            if (filter.expiresTo() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("expiresAt"), filter.expiresTo()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private static boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
