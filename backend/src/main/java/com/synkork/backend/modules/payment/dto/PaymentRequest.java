package com.synkork.backend.modules.payment.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentRequest {

    @NotNull(message = "Gói đăng ký không được bỏ trống")
    private String plan;

    @NotNull(message = "Chu kỳ gói không được bỏ trống")
    private String billingCycle;
}