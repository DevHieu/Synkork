package com.synkork.backend.modules.report.dtos;

import com.synkork.backend.modules.report.enums.ReportReasonEnums;

public record ReportRequestDto(String targetId, ReportReasonEnums reason, String description) {}
