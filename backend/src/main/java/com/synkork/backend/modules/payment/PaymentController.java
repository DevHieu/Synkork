package com.synkork.backend.modules.payment;

import com.synkork.backend.modules.payment.dto.PaymentRequest;
import com.synkork.backend.modules.payment.dto.PaymentResponse;
import lombok.RequiredArgsConstructor;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/momo")
    public PaymentResponse createMomoPayment(@RequestBody PaymentRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        // Sau 5 giây backend tự gọi callback giả lập
        CompletableFuture.delayedExecutor(5, TimeUnit.SECONDS).execute(() -> {
            Map<String, Object> fakePayload = new HashMap<>();
            fakePayload.put("resultCode", "0");
            fakePayload.put("extraData",
                Base64.getEncoder().encodeToString(
                    (request.getPlan() + "|" + email).getBytes()
                )
            );
            paymentService.handleMomoCallback(fakePayload);
        });

        return paymentService.createMomoPayment(request.getPlan(), email);
    }

    @PostMapping("/momo/callback")
    public ResponseEntity<String> handleMomoCallback(@RequestBody Map<String, Object> payload) {
        paymentService.handleMomoCallback(payload);
        return ResponseEntity.ok("OK");
    }
}