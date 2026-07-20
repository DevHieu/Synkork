package com.synkork.backend.modules.payment.entity;

import com.synkork.backend.common.base.BaseEntity;
import com.synkork.backend.modules.payment.enums.DiscountTypeEnum;
import com.synkork.backend.modules.user.enums.PlanEnum;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "promotions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PromotionEntity extends BaseEntity {

    // Mã khuyến mãi user nhập lúc thanh toán, vd "BLACKFRIDAY2026"
    @Column(nullable = false, unique = true, length = 50)
    private String code;

    // Mô tả hiển thị cho user, vd "Giảm giá Black Friday 2026"
    @Column(length = 255)
    private String description;

    // PERCENTAGE | FIXED_AMOUNT
    @Enumerated(EnumType.STRING)
    @Column(name = "discount_type", nullable = false, length = 20)
    private DiscountTypeEnum discountType;

    // Giá trị giảm: nếu PERCENTAGE thì là số nguyên vd 20 (= 20%),
    // nếu FIXED_AMOUNT thì là số tiền VNĐ vd 50000
    @Column(name = "discount_value", nullable = false, precision = 12, scale = 2)
    private BigDecimal discountValue;

    // Chỉ áp dụng cho 1 plan cụ thể; để NULL nghĩa là áp dụng cho tất cả plan
    @Enumerated(EnumType.STRING)
    @Column(name = "applicable_plan", length = 20)
    private PlanEnum applicablePlan;

    @Column(name = "start_at", nullable = false)
    private LocalDateTime startAt;

    @Column(name = "end_at", nullable = false)
    private LocalDateTime endAt;

    // Giới hạn tổng số lượt được dùng mã này (NULL = không giới hạn)
    @Column(name = "max_uses")
    private Integer maxUses;

    // Đếm số lượt đã dùng, tăng dần mỗi lần áp dụng thành công
    @Column(name = "used_count", nullable = false)
    @Builder.Default
    private int usedCount = 0;

    // Cho phép tắt khuyến mãi thủ công mà không cần xóa record
    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    /**
     * Kiểm tra mã còn hiệu lực tại thời điểm hiện tại hay không
     * (chưa hết hạn, chưa vượt giới hạn lượt dùng, đang bật).
     */
    public boolean isValidAt(LocalDateTime now) {
        boolean withinTime = !now.isBefore(startAt) && !now.isAfter(endAt);
        boolean withinUsage = maxUses == null || usedCount < maxUses;
        return active && withinTime && withinUsage;
    }
}