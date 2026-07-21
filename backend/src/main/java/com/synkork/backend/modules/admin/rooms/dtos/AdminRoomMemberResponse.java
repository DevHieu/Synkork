package com.synkork.backend.modules.admin.rooms.dtos;

import com.synkork.backend.modules.roomMember.RoomMemberEntity;
import com.synkork.backend.modules.roomMember.enums.RoomMemberRoleEnum;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class AdminRoomMemberResponse {
    private UUID id;
    private String email;
    private String username;
    private String avatar;
    private RoomMemberRoleEnum role;
    private LocalDateTime joinedAt;

    public AdminRoomMemberResponse(RoomMemberEntity member) {
        this.id = member.getId();
        this.email = member.getUser().getEmail();
        this.username = member.getUser().getUsername();
        this.avatar = member.getUser().getAvatarUrl();
        this.role = member.getRole();
        this.joinedAt = member.getJoinedAt();
    }
}