package com.synkork.backend.modules.admin.statistics.dtos;

import com.synkork.backend.modules.user.enums.PlanEnum;

public record UserPlanCount(
        long count,
        PlanEnum plan
) {
}
