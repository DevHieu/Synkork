package com.synkork.backend.modules.admin.users.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateStatusRequest {

    @NotBlank(message = "status không được để trống")
    @Pattern(
            regexp = "(?i)active|inactive|banned",
            message = "status phải là: active, inactive, banned"
    )
    private String status;
}