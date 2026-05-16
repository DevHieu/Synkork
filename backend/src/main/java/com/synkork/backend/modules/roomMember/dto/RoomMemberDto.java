package com.synkork.backend.modules.roomMember.dto;

import com.synkork.backend.modules.roomMember.enums.RoomMemberRoleEnum;
import com.synkork.backend.modules.roomMember.RoomMemberEntity;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
public class RoomMemberDto {
    private UUID memberId;
    private String displayName;
    private String username;
    private String avatarUrl;
    private RoomMemberRoleEnum role;
    private boolean muted;
    private boolean deafen;

    public RoomMemberDto(RoomMemberEntity entity) {
        this.memberId = entity.getId();
        this.displayName = entity.getUser().getDisplayName();
        this.username = entity.getUser().getUsername();
        this.avatarUrl = entity.getUser().getAvatarUrl();
        this.role = entity.getRole();
        this.muted = entity.isMuted();
        this.deafen = entity.isDeafen();
    }

    public RoomMemberDto(String displayName, String username, String avatarUrl, RoomMemberRoleEnum role) {
        this.displayName = displayName;
        this.username = username;
        this.avatarUrl = avatarUrl;
        this.role = role;
    }
}
