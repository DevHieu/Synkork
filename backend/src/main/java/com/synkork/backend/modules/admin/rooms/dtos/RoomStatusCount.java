package com.synkork.backend.modules.admin.rooms.dtos;

import com.synkork.backend.modules.room.enums.RoomStatusEnum;

public record RoomStatusCount(
        long count,
        RoomStatusEnum status
) {
}
