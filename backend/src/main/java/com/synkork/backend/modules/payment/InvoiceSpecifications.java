package com.synkork.backend.modules.payment;

import com.synkork.backend.modules.payment.enums.InvoiceStatusEnum;
import com.synkork.backend.modules.user.enums.PlanEnum;
import lombok.experimental.UtilityClass;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

@UtilityClass
public class InvoiceSpecifications {

    public static Specification<InvoiceEntity> fetchUser() {
        return (root, query, cb) -> {
            if (Long.class != query.getResultType() && long.class != query.getResultType()) {
                root.fetch("user", jakarta.persistence.criteria.JoinType.LEFT);
            }
            return cb.conjunction();
        };
    }

    public static Specification<InvoiceEntity> hasStatus(InvoiceStatusEnum status) {
        return (root, query, cb) -> status == null ? cb.conjunction() : cb.equal(root.get("status"), status);
    }

    public static Specification<InvoiceEntity> hasPlan(PlanEnum plan) {
        return (root, query, cb) -> plan == null ? cb.conjunction() : cb.equal(root.get("user").get("currentPlan"), plan);
    }

    public static Specification<InvoiceEntity> hasPaymentMethod(String paymentMethod) {
        return (root, query, cb) -> {
            if (paymentMethod == null || paymentMethod.isBlank()) {
                return cb.conjunction();
            }
            return cb.equal(cb.lower(root.get("paymentMethod").as(String.class)), paymentMethod.trim().toLowerCase());
        };
    }

    public static Specification<InvoiceEntity> hasEmail(String email) {
        return (root, query, cb) -> {
            if (email == null || email.isBlank()) {
                return cb.conjunction();
            }
            return cb.like(cb.lower(root.get("user").get("email")), "%" + email.trim().toLowerCase() + "%");
        };
    }

    public static Specification<InvoiceEntity> hasUsername(String username) {
        return (root, query, cb) -> {
            if (username == null || username.isBlank()) {
                return cb.conjunction();
            }
            return cb.like(cb.lower(root.get("user").get("username")), "%" + username.trim().toLowerCase() + "%");
        };
    }

    public static Specification<InvoiceEntity> createdAtBetween(LocalDateTime startDate, LocalDateTime endDate) {
        return (root, query, cb) -> {
            if (startDate != null && endDate != null) {
                return cb.between(root.get("createdAt"), startDate, endDate);
            }
            if (startDate != null) {
                return cb.greaterThanOrEqualTo(root.get("createdAt"), startDate);
            }
            if (endDate != null) {
                return cb.lessThanOrEqualTo(root.get("createdAt"), endDate);
            }
            return cb.conjunction();
        };
    }
}

