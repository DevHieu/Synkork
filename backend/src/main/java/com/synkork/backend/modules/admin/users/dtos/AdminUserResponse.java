package com.synkork.backend.modules.admin.users.dtos;

import com.synkork.backend.modules.user.UserEntity;
import com.synkork.backend.modules.user.enums.PlanEnum;
import com.synkork.backend.modules.user.enums.RoleEnum;
import com.synkork.backend.modules.user.enums.UserStatusEnum;

import java.time.LocalDateTime;
import java.util.UUID;

public record AdminUserResponse(
        UUID id,
        String username,
        String displayName,
        String email,
        String avatarUrl,
        RoleEnum role,
        PlanEnum plan,
        UserStatusEnum status,
        String provider,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        int warning
) {
    public static AdminUserResponse from(UserEntity u) {
        return new AdminUserResponse(
                u.getId(),
                u.getUsername(),
                u.getDisplayName(),
                u.getEmail(),
                u.getAvatarUrl(),
                u.getRole() != null ? u.getRole() : null,
                u.getCurrentPlan() != null ? u.getCurrentPlan() : null,
                u.getStatus() != null ? u.getStatus() : null,
                u.getProvider() != null ? u.getProvider().name().toLowerCase() : null,
                u.getCreatedAt(),
                u.getUpdatedAt(),
                u.getWarning()
        );
    }
}
