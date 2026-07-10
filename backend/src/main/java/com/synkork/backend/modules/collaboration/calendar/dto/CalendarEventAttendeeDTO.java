package com.synkork.backend.modules.collaboration.calendar.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

import com.synkork.backend.modules.roomMember.RoomMemberEntity;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CalendarEventAttendeeDTO {
    private UUID memberId;
    private String username;
    private String displayName;
    private String avatarUrl;

    public CalendarEventAttendeeDTO(RoomMemberEntity member) {
        this.memberId = member.getId();
        if (member.getUser() != null) {
            this.username = member.getUser().getUsername();
            this.displayName = member.getUser().getDisplayName();
            this.avatarUrl = member.getUser().getAvatarUrl();
        }
    }
}
