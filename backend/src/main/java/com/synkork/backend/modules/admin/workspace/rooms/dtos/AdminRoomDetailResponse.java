package com.synkork.backend.modules.admin.workspace.rooms.dtos;

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
    private int memberCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private UUID ownerId;
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

        public OwnerDto(UserEntity user) {
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

        public MemberDto(RoomMemberEntity member) {
            this.id = member.getUser().getId();
            this.username = member.getUser().getUsername();
            this.email = member.getUser().getEmail();
            this.avatarUrl = member.getUser().getAvatarUrl();
            this.role = member.getRole() != null ? member.getRole().name() : null;
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
            this.type = space.getType() != null ? space.getType().name() : null;
        }
    }

    public AdminRoomDetailResponse(RoomEntity room) {
        this.id = room.getId();
        this.name = room.getName();
        this.avatarUrl = room.getAvatarUrl();
        this.description = room.getDescription();
        this.status = room.getStatus() != null ? room.getStatus().name() : null;
        this.type = room.getType() != null ? room.getType().name() : null;
        this.inviteCode = room.getInviteCode();
        this.createdAt = room.getCreatedAt();
        this.updatedAt = room.getUpdatedAt();

        if (room.getOwner() != null) {
            this.owner = new OwnerDto(room.getOwner());
            this.ownerId = room.getOwner().getId();
        }

        this.members = room.getRoomMembers() != null
                ? room.getRoomMembers().stream().map(MemberDto::new).toList()
                : List.of();
        this.memberCount = this.members.size();

        this.spaces = room.getSpaces() != null
                ? room.getSpaces().stream().map(SpaceDto::new).toList()
                : List.of();
    }
}