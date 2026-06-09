package com.synkork.backend.modules.admin.workspace.rooms.dtos;

import com.synkork.backend.modules.room.enums.RoomStatusEnum;
import lombok.Data;

@Data
public class AdminRoomRequest {
    private String name;
    private String description;
    private RoomStatusEnum status;
}