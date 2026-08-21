package com.synkork.backend.modules.space.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateSpaceRequest(
        @NotBlank(message = "Tên space không được bỏ trống")
        String name,

        @NotNull(message = "Loại space không được bỏ trống")
        String type) {
}
