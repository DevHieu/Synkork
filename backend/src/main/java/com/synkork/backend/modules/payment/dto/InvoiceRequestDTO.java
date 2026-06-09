package com.synkork.backend.modules.payment.dto;

import com.synkork.backend.modules.payment.enums.InvoiceStatusEnum;
import com.synkork.backend.modules.payment.enums.PaymentMethodEnum;
import com.synkork.backend.modules.user.enums.PlanEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceRequestDTO {
    private String userEmail;
    private BigDecimal amount;
    private PlanEnum plan;
    private InvoiceStatusEnum status;
    private PaymentMethodEnum paymentMethod;
    private String orderId;
}
