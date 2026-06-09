package com.synkork.backend.modules.admin.users.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;

public record UpdateUserRequest(
        String displayName,

        @Email(message = "email không hợp lệ")
        String email,

        @Pattern(
                regexp = "(?i)superadmin|admin|manager|cashier|user",
                message = "role phải là: superadmin, admin, manager, cashier, user"
        )
        String role,

        @Pattern(
                regexp = "(?i)active|inactive|invited|suspended|banned",
                message = "status phải là: active, inactive, invited, suspended, banned"
        )
        String status
) {}
