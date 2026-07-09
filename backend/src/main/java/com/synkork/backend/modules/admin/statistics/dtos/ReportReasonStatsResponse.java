package com.synkork.backend.modules.admin.statistics.dtos;

import com.synkork.backend.modules.report.enums.ReportReasonEnums;
import com.synkork.backend.modules.report.enums.ReportTypeEnums;

public record ReportReasonStatsResponse(
        ReportReasonEnums reason,
        ReportTypeEnums reportType,
        long count
) {}