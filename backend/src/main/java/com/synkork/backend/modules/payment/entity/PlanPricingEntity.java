package com.synkork.backend.modules.payment.entity;

import com.synkork.backend.common.base.BaseEntity;
import com.synkork.backend.modules.payment.enums.BillingCycleEnum;
import com.synkork.backend.modules.user.enums.PlanEnum;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "plan_pricings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlanPricingEntity extends BaseEntity {

    // TEAM | BUSINESS | ...
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PlanEnum plan;

    // MONTHLY | YEARLY
    @Enumerated(EnumType.STRING)
    @Column(name = "billing_cycle", nullable = false, length = 20)
    private BillingCycleEnum billingCycle;

    // Giá niêm yết (chưa áp khuyến mãi), đơn vị VNĐ (đồng, không phải xu)
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    // true = giá đang áp dụng hiện tại; false = giá cũ, giữ lại để xem lịch sử
    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;
}
