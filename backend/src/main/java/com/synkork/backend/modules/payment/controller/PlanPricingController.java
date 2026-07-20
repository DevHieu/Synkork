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

    /**
     * ADMIN ONLY — xem lịch sử giá (cả giá cũ đã tắt) của 1 plan + chu kỳ.
     * GET /api/payment/plan-pricing/history?plan=TEAM&billingCycle=MONTHLY
     *
     * LƯU Ý: @PreAuthorize("hasRole('ADMIN')") giả định Spring Security đang cấu hình role
     * dạng "ROLE_ADMIN"/authority "ADMIN" và đã bật @EnableMethodSecurity trong SecurityConfig.
     * Nếu project đang check role theo cách khác (ví dụ tự viết filter/interceptor thủ công
     * dựa vào JwtFilter), thay annotation này bằng cách kiểm tra tương ứng của project.
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/history")
    public ResponseEntity<List<PlanPricingResponse>> getPricingHistory(
            @RequestParam PlanEnum plan,
            @RequestParam BillingCycleEnum billingCycle
    ) {
        return ResponseEntity.ok(planPricingService.getPricingHistory(plan, billingCycle));
    }

    /**
     * ADMIN ONLY — đổi giá cho 1 plan + chu kỳ.
     * Không ghi đè giá cũ, mà tắt giá cũ và tạo giá mới (giữ lịch sử).
     * PUT /api/payment/plan-pricing
     * Body: { "plan": "TEAM", "billingCycle": "MONTHLY", "amount": 79000 }
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping
    public ResponseEntity<PlanPricingResponse> changePrice(@Valid @RequestBody PlanPricingRequest request) {
        return ResponseEntity.ok(planPricingService.changePrice(request));
    }
}