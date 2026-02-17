package com.synkork.backend.modules.user.dto;

import com.synkork.backend.modules.user.UserEntity;
import com.synkork.backend.modules.user.enums.RoleEnum;

public record UserInfoDto(String username, String displayName, String email, String avatarUrl, RoleEnum role) {
    public UserInfoDto(UserEntity user) {
        this(
                user.getUsername(),
                user.getDisplayName(),
                user.getEmail(),
                user.getAvatarUrl(),
                user.getRole()
        );
    }
}
