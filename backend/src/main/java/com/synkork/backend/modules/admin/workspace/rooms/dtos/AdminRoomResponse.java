package com.synkork.backend.modules.admin.workspace.rooms.dtos;

import com.synkork.backend.modules.room.RoomEntity;
import com.synkork.backend.modules.room.enums.RoomStatusEnum;
import com.synkork.backend.modules.room.enums.RoomTypeEnum;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class AdminRoomResponse {
    private UUID id;
    private String name;
    private String avatarUrl;
    private String description;
    private RoomStatusEnum status;
    private int memberCount;
    private LocalDateTime createdAt;

    public AdminRoomResponse(RoomEntity room) {
        this.id = room.getId();
        this.name = room.getName();
        this.avatarUrl = room.getAvatarUrl();
        this.description = room.getDescription();
        this.status = room.getStatus();
        this.memberCount = room.getRoomMembers() != null ? room.getRoomMembers().size() : 0;
        this.createdAt = room.getCreatedAt();
    }
}