package com.synkork.backend.modules.statistics.dtos;

public record OverviewStatsData(
        long totalUsers,
        long userOnlines,
        long totalRooms,
        long totalSubscriptions,

        // % so với hôm qua
        double userDayGrowth,
        double roomDayGrowth,
        double subscriptionDayGrowth,
        double onlineDayGrowth,

        // % so với tháng trước
        double userMonthGrowth,
        double roomMonthGrowth,
        double subscriptionMonthGrowth
) {}