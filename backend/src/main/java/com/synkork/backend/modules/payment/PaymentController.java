package com.synkork.backend.modules.payment;

import com.synkork.backend.modules.payment.dto.PaymentRequest;
import com.synkork.backend.modules.payment.dto.PaymentResponse;
import lombok.RequiredArgsConstructor;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payment")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @PostMapping("/momo")
    public ResponseEntity<Map<String, Object>> createMomoPayment(@RequestBody PaymentRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Map<String, Object> result = paymentService.createMomoPayment(request.getPlan(), request.getBillingCycle(), email);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/momo/callback")
    public ResponseEntity<String> handleMomoCallback(@RequestBody Map<String, Object> payload) {
        paymentService.handleMomoCallback(payload);
        return ResponseEntity.ok("OK");
    }
}