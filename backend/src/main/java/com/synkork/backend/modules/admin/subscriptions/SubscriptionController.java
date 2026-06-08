package com.synkork.backend.modules.admin.subscriptions;

import com.synkork.backend.modules.admin.subscriptions.dto.BillingDTO;
import com.synkork.backend.modules.admin.subscriptions.dto.BillingRequestDTO;
import com.synkork.backend.modules.payment.enums.InvoiceStatusEnum;
import com.synkork.backend.modules.payment.enums.PaymentMethodEnum;
import com.synkork.backend.modules.user.enums.PlanEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/manage/subscriptions")
public class SubscriptionController {

    @Autowired
    private SubscriptionService subscriptionService;

    @GetMapping("/billings")
    public ResponseEntity<Page<BillingDTO>> getBillings(
            @RequestParam(required = false) InvoiceStatusEnum status,
            @RequestParam(required = false) PlanEnum plan,
            @RequestParam(required = false) PaymentMethodEnum paymentMethod,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(subscriptionService.getBillings(status, plan, paymentMethod, email, username, startDate, endDate, page, size));
    }

    @GetMapping("/billings/{id}")
    public ResponseEntity<BillingDTO> getBillingById(@PathVariable UUID id) {
        return ResponseEntity.ok(subscriptionService.getBillingById(id));
    }

    @PostMapping("/billings")
    public ResponseEntity<BillingDTO> createBilling(@RequestBody BillingRequestDTO request) {
        return ResponseEntity.ok(subscriptionService.createBilling(request));
    }

    @PutMapping("/billings/{id}")
    public ResponseEntity<BillingDTO> updateBilling(@PathVariable UUID id, @RequestBody BillingRequestDTO request) {
        return ResponseEntity.ok(subscriptionService.updateBilling(id, request));
    }

    @DeleteMapping("/billings/{id}")
    public ResponseEntity<Void> deleteBilling(@PathVariable UUID id) {
        subscriptionService.deleteBilling(id);
        return ResponseEntity.noContent().build();
    }
}
