package com.synkork.backend.modules.admin.rooms.dtos;

import com.synkork.backend.modules.room.RoomEntity;
import com.synkork.backend.modules.roomMember.RoomMemberEntity;
import com.synkork.backend.modules.space.SpaceEntity;
import com.synkork.backend.modules.user.UserEntity;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class AdminRoomDetailResponse {
    private UUID id;
    private String name;
    private String avatarUrl;
    private String description;
    private String status;
    private String type;
    private String inviteCode;
    private int warning;
    private long memberCount;
    private long spaceCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private UUID ownerId;
    private OwnerDto owner;

    @Getter
    @Setter
    public static class OwnerDto {
        private UUID id;
        private String username;
        private String email;
        private String avatarUrl;

        public OwnerDto(UserEntity user) {
            this.id = user.getId();
            this.username = user.getUsername();
            this.email = user.getEmail();
            this.avatarUrl = user.getAvatarUrl();
        }
    }


    public AdminRoomDetailResponse(RoomEntity room, UserEntity owner, long memberCount, long spaceCount) {
        this.id = room.getId();
        this.name = room.getName();
        this.avatarUrl = room.getAvatarUrl();
        this.description = room.getDescription();
        this.status = room.getStatus() != null ? room.getStatus().name() : null;
        this.type = room.getType() != null ? room.getType().name() : null;
        this.inviteCode = room.getInviteCode();
        this.warning = room.getWarning();
        this.createdAt = room.getCreatedAt();
        this.updatedAt = room.getUpdatedAt();

        if (owner != null) {
            this.owner = new OwnerDto(owner);
            this.ownerId = owner.getId();
        }

        this.memberCount = memberCount;
        this.spaceCount = spaceCount;
    }
}