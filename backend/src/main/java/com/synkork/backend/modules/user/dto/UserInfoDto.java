package com.synkork.backend.modules.user.dto;

import com.synkork.backend.modules.user.UserEntity;

import java.util.UUID;

public record UserInfoDto(UUID id, String username, String displayName, String email, String avatarUrl) {
    public UserInfoDto(UserEntity user) {
        this(
                user.getId(),
                user.getUsername(),
                user.getDisplayName(),
                user.getEmail(),
                user.getAvatarUrl()
        );
    }
}
