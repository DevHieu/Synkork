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
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvoiceDTO {
    private UUID id;
    private BigDecimal amount;
    private LocalDateTime paidAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private PlanEnum plan;
    private InvoiceStatusEnum status;
    private String transactionId;
    private PaymentMethodEnum paymentMethod;
    private String userEmail;
    private String username;
}
