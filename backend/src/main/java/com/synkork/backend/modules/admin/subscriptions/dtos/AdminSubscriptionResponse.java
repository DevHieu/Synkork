package com.synkork.backend.modules.admin.subscriptions.dtos;

import com.synkork.backend.modules.payment.entity.InvoiceEntity;
import com.synkork.backend.modules.payment.entity.UserSubscriptionEntity;
import com.synkork.backend.modules.payment.enums.InvoiceStatusEnum;
import com.synkork.backend.modules.payment.enums.PaymentMethodEnum;
import com.synkork.backend.modules.payment.enums.SubscriptionStatusEnum;
import com.synkork.backend.modules.user.UserEntity;
import com.synkork.backend.modules.user.enums.PlanEnum;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record AdminSubscriptionResponse(
        UUID id,
        UUID userId,
        String username,
        String userEmail,
        PlanEnum plan,
        SubscriptionStatusEnum status,
        LocalDateTime startedAt,
        LocalDateTime expiresAt,
        boolean autoRenew,
        boolean current,
        UUID invoiceId,
        BigDecimal invoiceAmount,
        InvoiceStatusEnum invoiceStatus,
        PaymentMethodEnum paymentMethod,
        String transactionId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static AdminSubscriptionResponse from(UserSubscriptionEntity subscription) {
        if (subscription == null) return null;

        UserEntity user = subscription.getUser();
        InvoiceEntity invoice = subscription.getInvoice();

        return new AdminSubscriptionResponse(
                subscription.getId(),
                user != null ? user.getId() : null,
                user != null ? user.getUsername() : null,
                user != null ? user.getEmail() : null,
                subscription.getPlan(),
                subscription.getStatus(),
                subscription.getStartedAt(),
                subscription.getExpiresAt(),
                subscription.isAutoRenew(),
                subscription.isCurrent(),
                invoice != null ? invoice.getId() : null,
                invoice != null ? invoice.getAmount() : null,
                invoice != null ? invoice.getStatus() : null,
                invoice != null ? invoice.getPaymentMethod() : null,
                invoice != null ? invoice.getTransactionId() : null,
                subscription.getCreatedAt(),
                subscription.getUpdatedAt()
        );
    }
}
