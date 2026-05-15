package com.synkork.backend.modules.user.dto;

public record ChangePasswordDto(
        String currentPassword,
        String newPassword
) {}