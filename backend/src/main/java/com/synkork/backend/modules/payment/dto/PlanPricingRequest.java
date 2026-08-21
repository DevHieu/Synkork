package com.synkork.backend.modules.payment.dto;

import com.synkork.backend.modules.payment.enums.BillingCycleEnum;
import com.synkork.backend.modules.payment.enums.DiscountTypeEnum;
import com.synkork.backend.modules.user.enums.PlanEnum;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PlanPricingRequest {

    @NotNull(message = "plan không được để trống")
    private PlanEnum plan;

    @NotNull(message = "billingCycle không được để trống")
    private BillingCycleEnum billingCycle;

    @NotNull(message = "amount không được để trống")
    @DecimalMin(value = "0", inclusive = true, message = "amount phải >= 0")
    private BigDecimal amount;

    private DiscountTypeEnum discountType;

    @DecimalMin(value = "0", inclusive = true, message = "discountValue phải >= 0")
    private BigDecimal discountValue;
}
