package com.synkork.backend.modules.admin.statistics.dtos;

public record UserStatsResponse(
        long totalUsers,
        long newUsersToday,
        double userGrowth
) {
}
