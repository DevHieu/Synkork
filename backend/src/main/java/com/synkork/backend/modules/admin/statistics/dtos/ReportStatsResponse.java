package com.synkork.backend.modules.admin.statistics.dtos;

public record ReportStatsResponse(
        long totalReports,
        long pendingReports,
        long resolvedReports,
        long dismissedReports,
        long userReports,
        long roomReports
) {}
