package com.synkork.backend.modules.admin.room.dto;

import com.synkork.backend.modules.room.RoomEntity;
import com.synkork.backend.modules.roomMember.RoomMemberEntity;
import com.synkork.backend.modules.space.SpaceEntity;
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
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private OwnerDto owner;
    private List<MemberDto> members;
    private List<SpaceDto> spaces;

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

    @Getter
    @Setter
    public static class MemberDto {
        private UUID id;
        private String username;
        private String email;
        private String avatarUrl;
        private String role;
        private LocalDateTime joinedAt;

        public MemberDto(RoomMemberEntity member) {
            this.id = member.getUser().getId();
            this.username = member.getUser().getUsername();
            this.email = member.getUser().getEmail();
            this.avatarUrl = member.getUser().getAvatarUrl();
            this.role = member.getRole().name();
            this.joinedAt = member.getJoinedAt();
        }
    }

    @Getter
    @Setter
    public static class SpaceDto {
        private UUID id;
        private String name;
        private String type;

        public SpaceDto(SpaceEntity space) {
            this.id = space.getId();
            this.name = space.getName();
            this.type = space.getType().name();
        }
    }

    public AdminRoomDetailResponse(RoomEntity room) {
        this.id = room.getId();
        this.name = room.getName();
        this.avatarUrl = room.getAvatarUrl();
        this.description = room.getDescription();
        this.status = room.getStatus().name();
        this.type = room.getType().name();
        this.createdAt = room.getCreatedAt();
        this.updatedAt = room.getUpdatedAt();

        this.owner = room.getOwner() != null ? new OwnerDto(room.getOwner()) : null;

        this.members = room.getRoomMembers() != null
                ? room.getRoomMembers().stream().map(MemberDto::new).toList()
                : List.of();

        this.spaces = room.getSpaces() != null
                ? room.getSpaces().stream().map(SpaceDto::new).toList()
                : List.of();
    }
}