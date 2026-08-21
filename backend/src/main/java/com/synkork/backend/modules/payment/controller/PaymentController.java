package com.synkork.backend.modules.payment.controller;

import java.util.HashMap;
import java.util.Map;

import com.synkork.backend.common.utils.AuthUtils;
import com.synkork.backend.modules.payment.service.VnpayPaymentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import com.synkork.backend.modules.payment.dto.PaymentRequest;
import com.synkork.backend.modules.payment.service.PaymentService;

@RestController 
@RequestMapping("/payment")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private VnpayPaymentService vnpayPaymentService;

    @Value("${vnpay.redirect-url}")
    private String redirectUrl;

    @PostMapping("/momo")
    public ResponseEntity<Map<String, Object>> createMomoPayment(@Valid @RequestBody PaymentRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Map<String, Object> result = paymentService.createMomoPayment(
                request.getPlan(),
                request.getBillingCycle(),
                email
        );
        return ResponseEntity.ok(result);
    }

    @PostMapping("/momo/callback")
    public ResponseEntity<String> handleMomoCallback(@RequestBody Map<String, Object> payload) {
        paymentService.handleMomoCallback(payload);
        return ResponseEntity.ok("OK");
    }

    @PostMapping("/vnpay")
    public ResponseEntity<Map<String, Object>> createVnPayPayment(
            @Valid @RequestBody PaymentRequest request,
            HttpServletRequest httpRequest) {
        String email = AuthUtils.getCurrentUsername();
        String clientIp = httpRequest.getRemoteAddr();
        Map<String, Object> result = vnpayPaymentService.createVnPayPayment(
                request.getPlan(),
                request.getBillingCycle(),
                email,
                clientIp
        );
        return ResponseEntity.ok(result);
    }

    @GetMapping("/vnpay-return")
    public ResponseEntity<Void> handleVnPayReturn(HttpServletRequest request) {
        Map<String, String> params = new HashMap<>();
        request.getParameterMap().forEach((k, v) -> params.put(k, v[0]));

        vnpayPaymentService.handleVnPayReturn(params);

        return ResponseEntity.status(302).header("Location", redirectUrl).build();
    }
}