package com.synkork.backend.modules.space.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateSpaceRequest(
        @NotBlank(message = "Tên space không được bỏ trống")
        String name,
        
        boolean restricted
) {}
