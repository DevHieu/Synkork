package com.synkork.backend.modules.admin.workspace.rooms.dtos;

import com.synkork.backend.modules.room.enums.RoomStatusEnum;

import java.util.UUID;

public record AdminRoomRequest(
        String name,
        String description,
        String avatarUrl,
        RoomStatusEnum status,
        UUID ownerId // chỉ áp dụng cho GROUP room
) {}