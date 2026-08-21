package com.synkork.backend.modules.admin.rooms.dtos;

import com.synkork.backend.modules.room.enums.RoomStatusEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AdminRoomRequest(

        @NotBlank(message = "Tên room không được bỏ trống")
        String name,

        String description,

        String avatarUrl,

        @NotNull(message = "Trạng thái room không được bỏ trống")
        RoomStatusEnum status,

        @NotNull(message = "Owner ID không được bỏ trống")
        UUID ownerId

) {}