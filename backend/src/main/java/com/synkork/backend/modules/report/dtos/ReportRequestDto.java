package com.synkork.backend.modules.report.dtos;

import com.synkork.backend.modules.report.enums.ReportReasonEnums;
import jakarta.validation.constraints.NotNull;

public record ReportRequestDto(
        @NotNull(message = "Id đối tượng bị tố cáo không được bỏ trống")
        String targetId,

        @NotNull(message = "Lí do không được bỏ trống")
        ReportReasonEnums reason,

        String description) {}
