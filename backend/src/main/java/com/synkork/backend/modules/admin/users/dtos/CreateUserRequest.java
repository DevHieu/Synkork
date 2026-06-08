package com.synkork.backend.modules.admin.users.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateUserRequest {

    @NotBlank(message = "firstName không được để trống")
    private String firstName;

    @NotBlank(message = "lastName không được để trống")
    private String lastName;

    @NotBlank(message = "username không được để trống")
    private String username;

    @NotBlank(message = "email không được để trống")
    @Email(message = "email không hợp lệ")
    private String email;

    @NotBlank(message = "role không được để trống")
    @Pattern(
            regexp = "(?i)admin|manager|user",
            message = "role phải là: admin, manager, user"
    )
    private String role;

    @NotBlank(message = "status không được để trống")
    @Pattern(
            regexp = "(?i)active|inactive|banned",
            message = "status phải là: active, inactive, banned"
    )
    private String status;
}