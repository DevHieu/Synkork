package com.synkork.backend.modules.room.dto;

import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

public record UpdateRoomRequest(

        @NotBlank(message = "Tên phòng không được bỏ trống")
        String name,

        String description,

        @RequestPart(required = false) MultipartFile imageFile) {
}
