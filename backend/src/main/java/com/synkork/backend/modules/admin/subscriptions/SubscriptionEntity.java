package com.synkork.backend.modules.admin.subscriptions;

import com.synkork.backend.modules.admin.subscriptions.dto.BillingRequestDTO;
import com.synkork.backend.modules.payment.enums.InvoiceStatusEnum;
import com.synkork.backend.modules.payment.enums.PaymentMethodEnum;
import com.synkork.backend.modules.user.UserEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "invoices")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubscriptionEntity {

    public static SubscriptionEntity from(UserEntity user, BillingRequestDTO request) {
        InvoiceStatusEnum status = request.getStatusEnum();
        return SubscriptionEntity.builder()
                .user(user)
                .amount(request.getAmount())
                .invoiceStatus(status)
                .paymentMethodEnum(request.getPaymentMethodEnum())
                .transactionId(request.getOrderId())
                .paidAt(status == InvoiceStatusEnum.PAID ? LocalDateTime.now() : null)
                .build();
    }

    @Id
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, columnDefinition = "BINARY(16)")
    private UserEntity user;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private InvoiceStatusEnum invoiceStatus;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private PaymentMethodEnum paymentMethodEnum;

    @Column(length = 255)
    private String transactionId;

    private LocalDateTime paidAt;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
