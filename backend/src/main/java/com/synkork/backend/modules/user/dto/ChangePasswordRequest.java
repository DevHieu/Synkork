package com.synkork.backend.modules.user.dto;

import jakarta.validation.constraints.NotBlank;

public record ChangePasswordRequest(

        @NotBlank(message = "Mật khẩu hiện tại không được bỏ trống")
        String currentPassword,

        @NotBlank(message = "Mật khẩu mới không được bỏ trống")
        String newPassword
) {}