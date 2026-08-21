package com.synkork.backend.modules.admin.subscriptions.controller;

import com.synkork.backend.modules.payment.dto.PlanPricingRequest;
import com.synkork.backend.modules.payment.dto.PlanPricingResponse;
import com.synkork.backend.modules.payment.enums.BillingCycleEnum;
import com.synkork.backend.modules.payment.service.PlanPricingService;
import com.synkork.backend.modules.user.enums.PlanEnum;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller("/manage/plan-pricing")
public class PlanPricingController {

    @Autowired
    private PlanPricingService planPricingService;

    @GetMapping("/history")
    public ResponseEntity<List<PlanPricingResponse>> getPricingHistory(
            @RequestParam PlanEnum plan,
            @RequestParam BillingCycleEnum billingCycle
    ) {
        return ResponseEntity.ok(planPricingService.getPricingHistory(plan, billingCycle));
    }

    @PutMapping
    public ResponseEntity<PlanPricingResponse> changePrice(@Valid @RequestBody PlanPricingRequest request) {
        return ResponseEntity.ok(planPricingService.changePrice(request));
    }
}
