package com.synkork.backend.modules.payment.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentRequest {
    private String plan;
    private String billingCycle;
}