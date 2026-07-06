package com.synkork.backend.modules.admin.statistics.dtos;

import java.time.LocalDate;

public record ReportChartResponse(
        LocalDate date,
        long userReports,
        long roomReports
) {}