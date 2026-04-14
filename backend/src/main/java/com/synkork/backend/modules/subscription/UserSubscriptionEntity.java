package com.synkork.backend.modules.subscription;

import com.synkork.backend.modules.subscription.enums.BillingCycleEnum;
import com.synkork.backend.modules.subscription.enums.SubscriptionStatusEnum;
import com.synkork.backend.modules.user.UserEntity;
import com.synkork.backend.modules.user.enums.PlanEnum;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "user_subscriptions")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSubscriptionEntity {

    @Id
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, columnDefinition = "BINARY(16)")
    private UserEntity user;

    // VIP | VIP_PRO
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PlanEnum plan;

    // ACTIVE | EXPIRED | CANCELLED | TRIAL
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SubscriptionStatusEnum status;

    // MONTHLY | YEARLY
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BillingCycleEnum billingCycle;

    @Column(nullable = false)
    private LocalDateTime startedAt;

    // null nếu là FREE không hết hạn
    private LocalDateTime expiresAt;

    private LocalDateTime cancelledAt;

    @Column(nullable = false)
    private boolean autoRenew = true;
}