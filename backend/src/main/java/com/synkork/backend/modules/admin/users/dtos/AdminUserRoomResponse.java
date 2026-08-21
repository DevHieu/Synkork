package com.synkork.backend.modules.admin.users.dtos;

import com.synkork.backend.modules.room.RoomEntity;
import com.synkork.backend.modules.room.enums.RoomStatusEnum;
import com.synkork.backend.modules.room.enums.RoomTypeEnum;
import com.synkork.backend.modules.roomMember.RoomMemberEntity;
import com.synkork.backend.modules.roomMember.enums.MemberStatusEnum;
import com.synkork.backend.modules.roomMember.enums.RoomMemberRoleEnum;

import java.time.LocalDateTime;
import java.util.UUID;

public record AdminUserRoomResponse(
        UUID id,
        String name,
        String avatarUrl,
        String description,
        RoomTypeEnum type,
        RoomStatusEnum status,
        long memberCount,
        String inviteCode,
        UUID ownerId,
        String ownerUsername,
        int warning,
        RoomMemberRoleEnum memberRole,
        MemberStatusEnum memberStatus,
        LocalDateTime joinedAt,
        LocalDateTime createdAt
) {
    public static AdminUserRoomResponse from(RoomMemberEntity member, long memberCount) {
        RoomEntity room = member.getRoom();
        return new AdminUserRoomResponse(
                room.getId(),
                room.getName(),
                room.getAvatarUrl(),
                room.getDescription(),
                room.getType(),
                room.getStatus(),
                memberCount,
                room.getInviteCode(),
                room.getOwner() != null ? room.getOwner().getId() : null,
                room.getOwner() != null ? room.getOwner().getUsername() : null,
                room.getWarning(),
                member.getRole(),
                member.getStatus(),
                member.getJoinedAt(),
                room.getCreatedAt()
        );
    }
}
