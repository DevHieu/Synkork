package com.synkork.backend.modules.roomMember.dto;

import com.synkork.backend.modules.roomMember.enums.RoomMemberRoleEnum;
import com.synkork.backend.modules.roomMember.RoomMemberEntity;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RoomMemberDto {
    private String displayName;
    private String username;
    private String avatarUrl;
    private RoomMemberRoleEnum role;

    public RoomMemberDto(RoomMemberEntity entity) {
        this.displayName = entity.getUser().getDisplayName();
        this.username = entity.getUser().getUsername();
        this.avatarUrl = entity.getUser().getAvatarUrl();
        this.role = entity.getRole();
    }

    public RoomMemberDto(String displayName, String username, String avatarUrl, RoomMemberRoleEnum role) {
        this.displayName = displayName;
        this.username = username;
        this.avatarUrl = avatarUrl;
        this.role = role;
    }
}
