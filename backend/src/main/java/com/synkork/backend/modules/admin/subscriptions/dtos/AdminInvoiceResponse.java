package com.synkork.backend.modules.admin.subscriptions.dtos;

import com.synkork.backend.modules.payment.entity.InvoiceEntity;
import com.synkork.backend.modules.payment.enums.BillingCycleEnum;
import com.synkork.backend.modules.payment.enums.InvoiceStatusEnum;
import com.synkork.backend.modules.payment.enums.PaymentMethodEnum;
import com.synkork.backend.modules.user.enums.PlanEnum;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record AdminInvoiceResponse(
        UUID id,
        BigDecimal amount,
        LocalDateTime paidAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        PlanEnum plan,
        BillingCycleEnum billingCycle,
        InvoiceStatusEnum status,
        String transactionId,
        PaymentMethodEnum paymentMethod,
        String userEmail,
        String username
) {
    public static AdminInvoiceResponse from(InvoiceEntity invoice) {
        if (invoice == null) return null;
        return new AdminInvoiceResponse(
                invoice.getId(),
                invoice.getAmount(),
                invoice.getPaidAt(),
                invoice.getCreatedAt(),
                invoice.getUpdatedAt(),
                invoice.getPlan(),
                invoice.getBillingCycle(),
                invoice.getStatus(),
                invoice.getTransactionId(),
                invoice.getPaymentMethod(),
                invoice.getUser() != null ? invoice.getUser().getEmail() : null,
                invoice.getUser() != null ? invoice.getUser().getUsername() : null
        );
    }
}
