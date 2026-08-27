package com.synkork.backend.modules.room.dto;

import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

public record CreateRoomRequest(
        @NotBlank(message = "Tên phòng không được bỏ trống")
        String name,

        @RequestPart(required = false) MultipartFile imageFile) {
}
