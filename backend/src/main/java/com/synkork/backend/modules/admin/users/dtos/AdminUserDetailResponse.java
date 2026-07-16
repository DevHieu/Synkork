package com.synkork.backend.modules.admin.users.dtos;

import com.synkork.backend.modules.user.UserEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record AdminUserDetailResponse(
        UUID id,
        String username,
        String displayName,
        String email,
        String avatarUrl,
        String role,
        String plan,
        String status,
        String provider,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        int warning,
        List<AdminUserRoomResponse> rooms
) {
    public static AdminUserDetailResponse from(UserEntity user, List<AdminUserRoomResponse> rooms) {
        AdminUserResponse base = AdminUserResponse.from(user);
        return new AdminUserDetailResponse(
                base.id(), base.username(), base.displayName(), base.email(), base.avatarUrl(),
                base.role(), base.plan(), base.status(), base.provider(), base.createdAt(),
                base.updatedAt(), base.warning(), rooms
        );
    }
}
