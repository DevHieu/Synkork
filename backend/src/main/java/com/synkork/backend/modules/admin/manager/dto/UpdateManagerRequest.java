package com.synkork.backend.modules.admin.manager.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateManagerRequest {

    private String displayName;

    @Email(message = "Email khong hop le")
    private String email;

    @Pattern(
            regexp = "(?i)active|inactive|banned",
            message = "Trang thai phai la active, inactive hoac banned"
    )
    private String status;

    @Pattern(
            regexp = "(?i)user|manager|admin",
            message = "Vai tro phai la user, manager hoac admin"
    )
    private String role;

    @Pattern(
            regexp = "(?i)free|team|business",
            message = "Goi dang ky phai la free, team hoac business"
    )
    private String plan;
}
