package com.synkork.backend.modules.admin.statistics.dtos;

import java.util.List;

public record UserDashboardChartResponse(
        List<UserStatusCount> statusCounts,
        List<UserPlanCount> planCounts
) {
}
