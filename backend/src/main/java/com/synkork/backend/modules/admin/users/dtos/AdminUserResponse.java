package com.synkork.backend.modules.admin.users.dtos;

import com.synkork.backend.modules.user.UserEntity;
import java.time.LocalDateTime;
import java.util.UUID;

public record AdminUserResponse(
        UUID id,
        String username,
        String displayName,
        String email,
        String avatarUrl,
        String plan,
        String status,
        String provider,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static AdminUserResponse from(UserEntity u) {
        return new AdminUserResponse(
                u.getId(),
                u.getUsername(),
                u.getDisplayName(),
                u.getEmail(),
                u.getAvatarUrl(),
                u.getCurrentPlan() != null ? u.getCurrentPlan().name() : null,
                u.getStatus() != null ? u.getStatus().name().toLowerCase() : null,
                u.getProvider() != null ? u.getProvider().name().toLowerCase() : null,
                u.getCreatedAt(),
                u.getUpdatedAt()
        );
    }
}
