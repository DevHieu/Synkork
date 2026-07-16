package com.synkork.backend.modules.payment.entity;

import com.synkork.backend.common.base.BaseEntity;
import com.synkork.backend.modules.payment.enums.BillingCycleEnum;
import com.synkork.backend.modules.user.enums.PlanEnum;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(
    name = "plan_pricings",
    uniqueConstraints = @UniqueConstraint(columnNames = {"plan", "billing_cycle"})
)
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

    // Giá tiền VNĐ (đơn vị đồng, không phải xu)
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    // Cho phép ẩn/tắt 1 gói mà không cần xóa record (giữ lịch sử giá cũ)
    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    // Ghi chú, vd "Giảm giá black friday 2026"
    private String note;
}
