package com.synkork.backend.modules.payment.entity;

import com.synkork.backend.common.base.BaseEntity;
import com.synkork.backend.modules.payment.enums.SubscriptionStatusEnum;
import com.synkork.backend.modules.user.UserEntity;
import com.synkork.backend.modules.user.enums.PlanEnum;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_subscriptions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSubscriptionEntity extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, columnDefinition = "BINARY(16)")
    private UserEntity user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PlanEnum plan;

    // ACTIVE | EXPIRED | CANCELLED | ...
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private SubscriptionStatusEnum status = SubscriptionStatusEnum.ACTIVE;

    @Column(nullable = false)
    private LocalDateTime startedAt;

    private LocalDateTime expiresAt;

    @Column(nullable = false)
    @Builder.Default
    private boolean autoRenew = false;

    // Gói này được kích hoạt từ invoice nào (nullable vì có thể là gói FREE mặc định)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id", columnDefinition = "BINARY(16)")
    private InvoiceEntity invoice;

    @Column(name = "is_current", nullable = false)
    @Builder.Default
    private boolean current = false;
}