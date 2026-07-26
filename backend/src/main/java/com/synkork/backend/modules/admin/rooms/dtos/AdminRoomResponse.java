package com.synkork.backend.modules.admin.rooms.dtos;

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
    private RoomTypeEnum type;
    private RoomStatusEnum status;
    private int memberCount;
    private String inviteCode;
    private LocalDateTime createdAt;

    private UUID ownerId;
    private String ownerUsername;

    private int warning;

    public AdminRoomResponse(RoomEntity room) {
        this.id = room.getId();
        this.name = room.getName();
        this.avatarUrl = room.getAvatarUrl();
        this.description = room.getDescription();
        this.type = room.getType();
        this.status = room.getStatus();
        this.memberCount = room.getRoomMembers() != null ? room.getRoomMembers().size() : 0;
        this.inviteCode = room.getInviteCode();
        this.createdAt = room.getCreatedAt();

        if (room.getOwner() != null) {
            this.ownerId = room.getOwner().getId();
            this.ownerUsername = room.getOwner().getUsername();
        }

        this.warning = room.getWarning();
    }
}