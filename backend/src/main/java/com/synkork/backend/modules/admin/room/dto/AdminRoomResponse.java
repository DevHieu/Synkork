package com.synkork.backend.modules.admin.room.dto;

import com.synkork.backend.modules.room.RoomEntity;
import com.synkork.backend.modules.room.enums.RoomStatusEnum;
import com.synkork.backend.modules.room.enums.RoomTypeEnum;
import lombok.Data;

import java.util.UUID;

@Data
public class AdminRoomResponse {
    private UUID id;
    private String name;
    private String avatarUrl;
    private String description;
    private RoomTypeEnum type;
    private RoomStatusEnum status;
    private String inviteCode;
    private int memberCount;

    // ✅ constructor nhận RoomEntity
    public AdminRoomResponse(RoomEntity room) {
        this.id = room.getId();
        this.name = room.getName();
        this.avatarUrl = room.getAvatarUrl();
        this.description = room.getDescription();
        this.type = room.getType();
        this.status = room.getStatus();
        this.inviteCode = room.getInviteCode();
        this.memberCount = room.getRoomMembers() != null ? room.getRoomMembers().size() : 0;
    }
}