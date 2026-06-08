package com.synkork.backend.modules.admin.users.dtos;

import com.synkork.backend.modules.user.UserEntity;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class AdminUserResponse {

    private UUID id;
    private String username;
    private String displayName;
    private String email;
    private String avatarUrl;
    private String role;
    private String status;
    private String provider;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static AdminUserResponse from(UserEntity u) {
        return AdminUserResponse.builder()
                .id(u.getId())
                .username(u.getUsername())
                .displayName(u.getDisplayName())
                .email(u.getEmail())
                .avatarUrl(u.getAvatarUrl())
                .role(u.getRole() != null ? u.getRole().name().toLowerCase() : null)
                .status(u.getStatus() != null ? u.getStatus().name().toLowerCase() : null)
                .provider(u.getProvider() != null ? u.getProvider().name().toLowerCase() : null)
                .createdAt(u.getCreatedAt())
                .updatedAt(u.getUpdatedAt())
                .build();
    }
}