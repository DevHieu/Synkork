package com.synkork.backend.modules.payment.dto;

import com.synkork.backend.modules.payment.enums.BillingCycleEnum;
import com.synkork.backend.modules.user.enums.PlanEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlanPricingResponse {
    private UUID id;
    private PlanEnum plan;
    private BillingCycleEnum billingCycle;
    private BigDecimal amount;
    private boolean active;
    private LocalDateTime createdAt;
}