package com.synkork.backend.modules.admin.workspace.rooms.dtos;

import com.synkork.backend.modules.room.RoomEntity;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
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
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private OwnerDto owner;

    @Getter
    @Setter
    public static class OwnerDto {
        private UUID id;
        private String username;
        private String email;
        private String avatarUrl;

        public OwnerDto(com.synkork.backend.modules.user.UserEntity user) {
            this.id = user.getId();
            this.username = user.getUsername();
            this.email = user.getEmail();
            this.avatarUrl = user.getAvatarUrl();
        }
    }

    public AdminRoomDetailResponse(RoomEntity room) {
        this.id = room.getId();
        this.name = room.getName();
        this.avatarUrl = room.getAvatarUrl();
        this.description = room.getDescription();
        this.status = room.getStatus() != null ? room.getStatus().name() : null;
        this.type = room.getType() != null ? room.getType().name() : null;
        this.createdAt = room.getCreatedAt();
        this.updatedAt = room.getUpdatedAt();
        this.owner = room.getOwner() != null ? new OwnerDto(room.getOwner()) : null;
    }
}