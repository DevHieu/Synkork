package com.synkork.backend.modules.admin.workspace.rooms.dtos;

import com.synkork.backend.modules.user.UserEntity;
import lombok.Data;

import java.util.UUID;

@Data
public class AdminUserOptionResponse {
    private UUID id;
    private String username;
    private String email;
    private String avatarUrl;

    public AdminUserOptionResponse(UserEntity user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.email = user.getEmail();
        this.avatarUrl = user.getAvatarUrl();
    }
} 