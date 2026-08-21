package com.synkork.backend.modules.admin.users.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record CreateUserRequest(
        @NotBlank(message = "firstName không được để trống")
        String firstName,

        @NotBlank(message = "lastName không được để trống")
        String lastName,

        @NotBlank(message = "username không được để trống")
        String username,

        @NotBlank(message = "email không được để trống")
        @Email(message = "email không hợp lệ")
        String email,

        @NotBlank(message = "status không được để trống")
        @Pattern(
                regexp = "(?i)active|banned",
                message = "status phải là: active, banned"
        )
        String status,

        @Pattern(
                regexp = "(?i)free|team|business",
                message = "plan phải là: free, team, business"
        )
        String plan,

        @Pattern(
                regexp = "(?i)user",
                message = "role must be user"
        )
        String role
) {}
