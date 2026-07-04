package com.synkork.backend.modules.admin.statistics.dtos;

public record UserStatsResponse(
        long totalUsers,
        long newUsersToday,
        long newUsersThisMonth,
        long activeUsers,
        long inactiveUsers,
        long bannedUsers,
        long freeUsers,
        long teamUsers,
        long businessUsers
) {
}
