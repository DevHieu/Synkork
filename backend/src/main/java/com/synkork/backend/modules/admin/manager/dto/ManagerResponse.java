package com.synkork.backend.modules.admin.manager.dto;

import com.synkork.backend.modules.user.UserEntity;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class ManagerResponse {

    private UUID id;
    private String username;
    private String displayName;
    private String email;
    private String avatarUrl;
    private String role;
    private String status;
    private String plan;
    private String provider;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ManagerResponse from(UserEntity account) {
        return ManagerResponse.builder()
                .id(account.getId())
                .username(account.getUsername())
                .displayName(account.getDisplayName())
                .email(account.getEmail())
                .avatarUrl(account.getAvatarUrl())
                .role(account.getRole() != null ? account.getRole().name().toLowerCase() : null)
                .status(account.getStatus() != null ? account.getStatus().name().toLowerCase() : null)
                .plan(account.getCurrentPlan() != null ? account.getCurrentPlan().name() : null)
                .provider(account.getProvider() != null ? account.getProvider().name().toLowerCase() : null)
                .createdAt(account.getCreatedAt())
                .updatedAt(account.getUpdatedAt())
                .build();
    }
}
