package com.synkork.backend.modules.admin.auditLog.dtos;

import com.synkork.backend.common.dtos.PageableFilter;
import com.synkork.backend.modules.admin.auditLog.enums.LogEntityTypeEnum;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.UUID;

public record AuditLogFilterRequest(
        String search,
        String action,
        LogEntityTypeEnum entityType,
        UUID workspaceId,
        String actorEmail,

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
