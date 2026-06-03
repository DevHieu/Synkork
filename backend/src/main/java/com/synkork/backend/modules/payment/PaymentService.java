package com.synkork.backend.modules.payment;

import java.util.Map;
import com.synkork.backend.modules.payment.dto.PaymentResponse;

public interface PaymentService {
    PaymentResponse createMomoPayment(String plan, String userEmail);
    void handleMomoCallback(Map<String, Object> payload);
}