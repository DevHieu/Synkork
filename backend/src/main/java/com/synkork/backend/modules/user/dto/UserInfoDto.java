package com.synkork.backend.modules.user.dto;

import com.synkork.backend.modules.user.UserEntity;
import com.synkork.backend.modules.user.enums.PlanEnum;

import java.util.UUID;

public record UserInfoDto(UUID id, String username, String displayName, String email, String avatarUrl, String provider, PlanEnum currentPlan) {
    public UserInfoDto(UserEntity user) {
        this(
                user.getId(),
                user.getUsername(),
                user.getDisplayName(),
                user.getEmail(),
                user.getAvatarUrl(),
                user.getProvider() != null ? user.getProvider().name() : "LOCAL",
                user.getCurrentPlan()
        );
    }
}