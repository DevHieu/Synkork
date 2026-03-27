package com.synkork.backend.modules.user.dto;

import com.synkork.backend.modules.roomMember.enums.RoomMemberRoleEnum;

public record SenderDto(String username, String displayName, String avatarUrl, RoomMemberRoleEnum role) {
}
