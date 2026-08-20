package com.synkork.backend.modules.payment.controller;

import com.synkork.backend.modules.payment.dto.PlanPricingResponse;
import com.synkork.backend.modules.payment.dto.PlanPricingRequest;
import com.synkork.backend.modules.payment.enums.BillingCycleEnum;
import com.synkork.backend.modules.payment.service.PlanPricingService;
import com.synkork.backend.modules.user.enums.PlanEnum;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/payment/plan-pricing")
public class PlanPricingController {

    @Autowired
    private PlanPricingService planPricingService;

    /**
     * PUBLIC — cho UI/frontend lấy bảng giá hiện tại thay vì hard-code giá trong code.
     * GET /api/payment/plan-pricing
     */
    @GetMapping
    public ResponseEntity<List<PlanPricingResponse>> getActivePricings() {
        return ResponseEntity.ok(planPricingService.getActivePricings());
    }

}