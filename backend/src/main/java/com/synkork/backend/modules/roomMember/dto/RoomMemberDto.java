package com.synkork.backend.modules.roomMember.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import com.synkork.backend.modules.roomMember.RoomMemberEntity;
import com.synkork.backend.modules.roomMember.enums.RoomMemberRoleEnum;

import lombok.Data;
import lombok.NoArgsConstructor;

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
    private LocalDateTime chatDisableUntil;

    public RoomMemberDto(RoomMemberEntity entity) {
        this.memberId = entity.getId();
        if (entity.getUser() != null) {
            this.displayName = entity.getUser().getDisplayName();
            this.username = entity.getUser().getUsername();
            this.avatarUrl = entity.getUser().getAvatarUrl();
        }
        this.role = entity.getRole();
        this.muted = entity.isMuted();
        this.deafen = entity.isDeafen();
        this.chatDisableUntil = entity.getChatDisableUntil();
    }

    public RoomMemberDto(UUID memberId, String displayName, String username, String avatarUrl, RoomMemberRoleEnum role) {
        this.memberId = memberId;
        this.displayName = displayName;
        this.username = username;
        this.avatarUrl = avatarUrl;
        this.role = role;
    }
}
