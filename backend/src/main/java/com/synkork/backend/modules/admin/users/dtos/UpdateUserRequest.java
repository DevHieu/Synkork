package com.synkork.backend.modules.admin.users.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;

public record UpdateUserRequest(
        String displayName,

        @Email(message = "email không hợp lệ")
        String email,

        @Pattern(
                regexp = "(?i)free|team|business",
                message = "plan phải là: free, team, business"
        )
        String plan,

        @Pattern(
                regexp = "(?i)active|inactive|invited|suspended|banned",
                message = "status phải là: active, inactive, invited, suspended, banned"
        )
        String status,

        @Pattern(
                regexp = "(?i)user|manager|admin",
                message = "role must be user, manager, or admin"
        )
        String role
) {}
