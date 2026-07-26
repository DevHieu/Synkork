package com.synkork.backend.modules.admin.rooms.dashboardroom;

import com.synkork.backend.modules.room.enums.RoomStatusEnum;

public record RoomStatusCount(
        long count,
        RoomStatusEnum status
) {
}
