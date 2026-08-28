package com.synkork.backend.modules.admin.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AdminChangePasswordRequest(

        @NotBlank(message = "Mật khẩu hiện tại không được bỏ trống")
        String currentPassword,

        @NotBlank(message = "Mật khẩu mới không được bỏ trống")
        @Size(min = 6, message = "Mật khẩu mới phải có ít nhất 6 ký tự")
        String newPassword
) {}
