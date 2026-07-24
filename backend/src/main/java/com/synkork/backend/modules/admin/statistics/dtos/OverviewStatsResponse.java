package com.synkork.backend.modules.admin.statistics.dtos;

public record OverviewStatsResponse(
        long totalUsers,
        long userOnlines,
        long totalRooms,
        long totalSubscriptions,

        // % so với kỳ liền trước (dateFrom -> dateTo)
        double userGrowth,
        double roomGrowth,
        double subscriptionGrowth,
        double onlineGrowth
) {}
