package com.synkork.backend.modules.admin.utils;

import com.synkork.backend.modules.user.enums.PlanEnum;

public class AdminUtils {
    public static double calcGrowth(long current, long previous) {
        System.out.println("current: " + current + " previous: " + previous);
        if (previous == 0)
            return current > 0 ? 100.0 : 0.0;
        return Math.round(((double) (current - previous) / previous) * 1000.0) / 10.0;
    }

    public static double calcRate(long current, long total) {
        if (total == 0)
            return 0.0;
        return Math.round(((double) current / total) * 1000.0) / 10.0;
    }

    public static boolean isPlanDowngrade(PlanEnum oldPlan, PlanEnum newPlan) {
        return planRank(newPlan) < planRank(oldPlan);
    }

    private static int planRank(PlanEnum plan) {
        if (plan == null) {
            return 1;
        }
        return switch (plan) {
            case FREE -> 1;
            case TEAM -> 2;
            case BUSINESS -> 3;
        };
    }
}
