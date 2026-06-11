package com.synkork.backend.modules.admin.workspace.rooms.dtos;

import com.synkork.backend.modules.room.enums.RoomStatusEnum;

public record AdminRoomRequest(String name, String description, RoomStatusEnum status) {}