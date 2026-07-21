package com.synkork.backend.modules.payment.entity;

import java.math.BigDecimal;

import com.synkork.backend.common.base.BaseEntity;
import com.synkork.backend.modules.payment.enums.BillingCycleEnum;
import com.synkork.backend.modules.payment.enums.DiscountTypeEnum;
import com.synkork.backend.modules.user.enums.PlanEnum;

import jakarta.persistence.*;
import lombok.*;

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

    // Giảm giá
    @Enumerated(EnumType.STRING)
    @Column(name = "discount_type", length = 20)
    private DiscountTypeEnum discountType;   // PERCENTAGE hoặc FIXED

    @Column(name = "discount_value", precision = 8, scale = 2)
    private BigDecimal discountValue;        // 20.00 = 20% hoặc 200000 = giảm 200k

    // Số tiền giảm thực tế (được tính toán và lưu lại khi áp dụng)
    @Column(name = "discount_amount", precision = 12, scale = 2)
    private BigDecimal discountAmount;
}
