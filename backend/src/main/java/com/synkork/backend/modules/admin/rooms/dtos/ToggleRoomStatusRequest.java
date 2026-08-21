package com.synkork.backend.modules.admin.rooms.dtos;

import com.synkork.backend.modules.room.enums.RoomStatusEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ToggleRoomStatusRequest(

        @NotNull(message = "Trạng thái room không được bỏ trống")
        RoomStatusEnum status,

        @NotBlank(message = "Lý do không được bỏ trống")
        String reason

) {}