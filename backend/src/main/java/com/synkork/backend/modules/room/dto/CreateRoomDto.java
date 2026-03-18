package com.synkork.backend.modules.room.dto;

import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

public record CreateRoomDto(String name, String ownerId, @RequestPart(required = false) MultipartFile imageFile) {
}
