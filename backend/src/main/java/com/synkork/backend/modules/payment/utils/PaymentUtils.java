package com.synkork.backend.modules.payment.utils;

import com.synkork.backend.modules.payment.entity.PlanPricingEntity;
import com.synkork.backend.modules.payment.enums.BillingCycleEnum;
import com.synkork.backend.modules.payment.enums.DiscountTypeEnum;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PaymentUtils {

    public static LocalDateTime resolveExpiresAt(LocalDateTime now, String billing) {
        BillingCycleEnum cycle = BillingCycleEnum.valueOf(billing.toUpperCase());
        return cycle == BillingCycleEnum.YEARLY ? now.plusYears(1) : now.plusMonths(1);
    }

    public static BigDecimal calculateFinalAmount(PlanPricingEntity pricing) {
        BigDecimal baseAmount = pricing.getAmount();
        BigDecimal discountAmount = BigDecimal.ZERO;

        if (pricing.getDiscountType() != null && pricing.getDiscountValue() != null) {
            if (pricing.getDiscountType() == DiscountTypeEnum.PERCENTAGE) {
                discountAmount = baseAmount
                        .multiply(pricing.getDiscountValue())
                        .divide(BigDecimal.valueOf(100), 0, BigDecimal.ROUND_HALF_UP);
            } else if (pricing.getDiscountType() == DiscountTypeEnum.FIXED) {
                discountAmount = pricing.getDiscountValue();
            }

            // Không cho giảm vượt quá giá gốc
            discountAmount = discountAmount.min(baseAmount);
        }

        if (pricing.getDiscountAmount() == null || !pricing.getDiscountAmount().equals(discountAmount)) {
            pricing.setDiscountAmount(discountAmount);
        }

        return baseAmount.subtract(discountAmount).max(BigDecimal.ZERO);
    }


}
