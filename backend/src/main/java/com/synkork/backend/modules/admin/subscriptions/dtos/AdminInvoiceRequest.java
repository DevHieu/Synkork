package com.synkork.backend.modules.admin.subscriptions.dtos;

import com.synkork.backend.modules.payment.enums.InvoiceStatusEnum;
import com.synkork.backend.modules.payment.enums.PaymentMethodEnum;
import com.synkork.backend.modules.user.enums.PlanEnum;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record AdminInvoiceRequest(
        @NotNull(message = "Email người dùng không được để trống")
        @Email(message = "Email không hợp lệ")
        String userEmail,

        @NotNull(message = "Số tiền không được để trống")
        @Positive(message = "Số tiền phải lớn hơn 0")
        BigDecimal amount,

        PlanEnum plan,
        InvoiceStatusEnum status,
        PaymentMethodEnum paymentMethod,
        String orderId
) {}
