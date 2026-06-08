package com.synkork.backend.modules.admin.subscriptions.dto;

import com.synkork.backend.modules.payment.enums.InvoiceStatusEnum;
import com.synkork.backend.modules.payment.enums.PaymentMethodEnum;
import com.synkork.backend.modules.user.enums.PlanEnum;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BillingDTO {
    private String id;
    private BigDecimal amount;
    private LocalDate date;
    private String plan;
    private String status;
    private String orderId;
    private String description;
    private String file;
    private String currency;
    private String invoiceNo;
    private String paymentMethod;
    private String userEmail;
    private String userName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Constructor dùng cho JPQL Constructor Expression
    public BillingDTO(
            UUID id,
            BigDecimal amount,
            LocalDateTime paidAt,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            PlanEnum currentPlan,
            InvoiceStatusEnum status,
            String transactionId,
            PaymentMethodEnum paymentMethod,
            String userEmail,
            String userName
    ) {
        this.id = id != null ? id.toString() : null;
        this.amount = amount;
        LocalDateTime dateTime = paidAt != null ? paidAt : createdAt;
        this.date = dateTime != null ? dateTime.toLocalDate() : null;
        this.plan = resolvePlan(currentPlan);
        this.status = resolveStatus(status);
        this.orderId = transactionId;
        this.description = "Invoice for " + this.plan + (transactionId != null && !transactionId.isBlank() ? ", order " + transactionId : "");
        this.file = null;
        this.currency = "VND";
        this.invoiceNo = this.id;
        this.paymentMethod = paymentMethod != null ? paymentMethod.name() : null;
        this.userEmail = userEmail;
        this.userName = userName;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    private String resolveStatus(InvoiceStatusEnum status) {
        if (status == null) return "unpaid";
        return switch (status) {
            case PAID -> "paid";
            case FAILED -> "cancelled";
            case PENDING -> "unpaid";
        };
    }

    private String resolvePlan(PlanEnum plan) {
        if (plan == null) return "Free";
        return switch (plan) {
            case FREE -> "Free";
            case TEAM -> "Small Business";
            case BUSINESS -> "Enterprise";
        };
    }
}
