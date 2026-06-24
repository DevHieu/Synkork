package com.synkork.backend.modules.admin.manager.dto;

import com.synkork.backend.common.dtos.PageableFilter;
import com.synkork.backend.modules.user.enums.RoleEnum;
import com.synkork.backend.modules.user.enums.UserStatusEnum;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

public record ManagerFilterRequest(
        String search,
        RoleEnum role,
        UserStatusEnum status,

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        LocalDateTime dateFrom,

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        LocalDateTime dateTo,

        @Min(value = 0, message = "Page phải >= 0")
        Integer page,

        @Min(value = 1, message = "Size phải >= 1")
        @Max(value = 100, message = "Size tối đa 100")
        Integer size
) implements PageableFilter {

    public void validate() {
        if (dateFrom != null && dateTo != null && dateFrom.isAfter(dateTo)) {
            throw new IllegalArgumentException("dateFrom phải nhỏ hơn hoặc bằng dateTo");
        }
    }
}