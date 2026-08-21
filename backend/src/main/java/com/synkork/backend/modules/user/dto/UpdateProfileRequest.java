package com.synkork.backend.modules.user.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateProfileRequest(

        @NotBlank(message = "Tên hiển thị không được bỏ trống")
        String displayName,

        @NotBlank(message = "Username không được bỏ trống")
        String username
) { }
