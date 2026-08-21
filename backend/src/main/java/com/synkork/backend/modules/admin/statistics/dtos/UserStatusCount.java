package com.synkork.backend.modules.admin.statistics.dtos;

import com.synkork.backend.modules.user.enums.UserStatusEnum;

public record UserStatusCount(
        long count,
        UserStatusEnum status
) {
}
