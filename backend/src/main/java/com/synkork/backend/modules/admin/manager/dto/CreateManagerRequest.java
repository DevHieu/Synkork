package com.synkork.backend.modules.admin.manager.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateManagerRequest {

    @NotBlank(message = "Tên hiển thị không được để trống")
    private String displayName;

    @NotBlank(message = "Username không được để trống")
    private String username;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email khong hop le")
    private String email;

    @NotBlank(message = "Trang thai khong duoc de trong")
    @Pattern(
            regexp = "(?i)active|not_verified|banned",
            message = "Trang thai phải là active, banned hoặc not_verified"
    )
    private String status;

    @NotBlank(message = "Vai tro khong duoc de trong")
    @Pattern(
            regexp = "(?i)manager|admin",
            message = "Vai tro phai la manager hoac admin"
    )
    private String role;
}
