    package com.synkork.backend.modules.admin.subscriptions;

import com.synkork.backend.modules.admin.subscriptions.dtos.InvoiceFilterRequest;
import com.synkork.backend.modules.payment.entity.InvoiceEntity;
import com.synkork.backend.modules.user.UserEntity;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

    import java.util.ArrayList;
    import java.util.List;
    import java.util.UUID;

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
                    String rawSearch = filter.search().trim();
                    String keyword = "%" + rawSearch.toLowerCase() + "%";

                    List<Predicate> searchPredicates = new ArrayList<>();
                    searchPredicates.add(cb.like(cb.lower(userJoin.get("username")), keyword));
                    searchPredicates.add(cb.like(cb.lower(userJoin.get("email")), keyword));

                    try {
                        UUID searchId = UUID.fromString(rawSearch);
                        searchPredicates.add(cb.equal(root.get("id"), searchId));
                    } catch (IllegalArgumentException ignored) {
                        // search string không phải UUID hợp lệ thì thôi, không match theo id
                    }

                    predicates.add(cb.or(searchPredicates.toArray(new Predicate[0])));
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
