package com.synkork.backend.modules.admin.manager.dto;

import com.synkork.backend.modules.user.UserEntity;
import com.synkork.backend.modules.user.enums.PlanEnum;
import com.synkork.backend.modules.user.enums.RoleEnum;
import com.synkork.backend.modules.user.enums.UserStatusEnum;
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
    private RoleEnum role;
    private UserStatusEnum status;
    private PlanEnum plan;
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
                .role(account.getRole() != null ? account.getRole() : null)
                .status(account.getStatus() != null ? account.getStatus() : null)
                .plan(account.getCurrentPlan() != null ? account.getCurrentPlan() : null)
                .provider(account.getProvider() != null ? account.getProvider().name().toLowerCase() : null)
                .createdAt(account.getCreatedAt())
                .updatedAt(account.getUpdatedAt())
                .build();
    }
}
