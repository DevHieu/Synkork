package com.synkork.backend.modules.admin.subscriptions.dtos;

import com.synkork.backend.common.dtos.PageableFilter;
import com.synkork.backend.modules.payment.enums.SubscriptionStatusEnum;
import com.synkork.backend.modules.user.enums.PlanEnum;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

public record AdminSubscriptionFilterRequest(
        String search,
        PlanEnum plan,
        SubscriptionStatusEnum status,
        Boolean current,

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        LocalDateTime expiresFrom,

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        LocalDateTime expiresTo,

        @Min(value = 0, message = "Page must be >= 0")
        Integer page,

        @Min(value = 1, message = "Size must be >= 1")
        @Max(value = 100, message = "Size must be <= 100")
        Integer size
) implements PageableFilter {
    public void validate() {
        if (expiresFrom != null && expiresTo != null && expiresFrom.isAfter(expiresTo)) {
            throw new IllegalArgumentException("expiresFrom must be before or equal to expiresTo");
        }
    }
}
