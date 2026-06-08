package com.synkork.backend.modules.admin.subscriptions.dto;

import com.synkork.backend.modules.payment.enums.InvoiceStatusEnum;
import com.synkork.backend.modules.payment.enums.PaymentMethodEnum;
import com.synkork.backend.modules.user.enums.PlanEnum;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BillingRequestDTO {
    private String userEmail;
    private BigDecimal amount;
    private String plan; // e.g. "Free", "Small Business", "Enterprise"
    private String status; // "paid", "cancelled", "unpaid"
    private String paymentMethod; // "MOMO", "VNPAY"
    private String orderId; // transactionId

    public PlanEnum getPlanEnum() {
        if (plan == null) return PlanEnum.FREE;
        return switch (plan.toLowerCase()) {
            case "small business", "team", "small_business" -> PlanEnum.TEAM;
            case "enterprise", "business" -> PlanEnum.BUSINESS;
            default -> PlanEnum.FREE;
        };
    }

    public InvoiceStatusEnum getStatusEnum() {
        if (status == null) return InvoiceStatusEnum.PENDING;
        return switch (status.toLowerCase()) {
            case "paid" -> InvoiceStatusEnum.PAID;
            case "cancelled", "failed" -> InvoiceStatusEnum.FAILED;
            default -> InvoiceStatusEnum.PENDING;
        };
    }

    public PaymentMethodEnum getPaymentMethodEnum() {
        if (paymentMethod == null) return null;
        try {
            return PaymentMethodEnum.valueOf(paymentMethod.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
