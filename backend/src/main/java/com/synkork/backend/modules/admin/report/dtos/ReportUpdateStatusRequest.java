package com.synkork.backend.modules.admin.report.dtos;

import com.synkork.backend.modules.report.enums.ReportStatusEnums;
import jakarta.validation.constraints.NotNull;

public record ReportUpdateStatusRequest(
        @NotNull(message = "Status không được để trống")
        ReportStatusEnums status,

        String note,
        
        Boolean hasWarn
) {}