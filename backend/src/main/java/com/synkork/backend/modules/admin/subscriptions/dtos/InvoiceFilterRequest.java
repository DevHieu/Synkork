package com.synkork.backend.modules.admin.subscriptions.dtos;

import com.synkork.backend.common.dtos.PageableFilter;
import com.synkork.backend.modules.payment.enums.BillingCycleEnum;
import com.synkork.backend.modules.payment.enums.InvoiceStatusEnum;
import com.synkork.backend.modules.payment.enums.PaymentMethodEnum;
import com.synkork.backend.modules.user.enums.PlanEnum;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

public record InvoiceFilterRequest(
        String search,
        InvoiceStatusEnum status,
        PlanEnum plan,
        BillingCycleEnum billingCycle,
        PaymentMethodEnum paymentMethod,

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        LocalDateTime dateFrom,

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        LocalDateTime dateTo,

        @Min(value = 0, message = "Page must be >= 0")
        Integer page,

        @Min(value = 1, message = "Size must be >= 1")
        @Max(value = 100, message = "Size must be <= 100")
        Integer size
) implements PageableFilter {
    public void validate() {
        if (dateFrom != null && dateTo != null && dateFrom.isAfter(dateTo)) {
            throw new IllegalArgumentException("dateFrom must be before or equal to dateTo");
        }
    }
}
