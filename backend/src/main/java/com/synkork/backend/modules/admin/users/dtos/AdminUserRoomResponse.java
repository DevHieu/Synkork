package com.synkork.backend.modules.admin.users.dtos;

import com.synkork.backend.modules.roomMember.RoomMemberEntity;

import java.time.LocalDateTime;
import java.util.UUID;

public record AdminUserRoomResponse(
        UUID membershipId,
        UUID roomId,
        String name,
        String type,
        String roomStatus,
        String role,
        String memberStatus,
        LocalDateTime joinedAt
) {
    public static AdminUserRoomResponse from(RoomMemberEntity member) {
        return new AdminUserRoomResponse(
                member.getId(),
                member.getRoom().getId(),
                member.getRoom().getName(),
                member.getRoom().getType() != null ? member.getRoom().getType().name() : null,
                member.getRoom().getStatus() != null ? member.getRoom().getStatus().name() : null,
                member.getRole() != null ? member.getRole().name() : null,
                member.getStatus() != null ? member.getStatus().name() : null,
                member.getJoinedAt()
        );
    }
}
