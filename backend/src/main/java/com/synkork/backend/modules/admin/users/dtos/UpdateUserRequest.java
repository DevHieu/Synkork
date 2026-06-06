package com.synkork.backend.modules.admin.users.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateUserRequest {

    private String displayName;

    @Email(message = "email không hợp lệ")
    private String email;

    @Pattern(
            regexp = "(?i)superadmin|admin|manager|cashier|user",
            message = "role phải là: superadmin, admin, manager, cashier, user"
    )
    private String role;

    @Pattern(
            regexp = "(?i)active|inactive|invited|suspended|banned",
            message = "status phải là: active, inactive, invited, suspended, banned"
    )
    private String status;
}