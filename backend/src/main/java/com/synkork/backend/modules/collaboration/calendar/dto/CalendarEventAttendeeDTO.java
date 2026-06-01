package com.synkork.backend.modules.collaboration.calendar.dto;

import com.synkork.backend.modules.user.UserEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CalendarEventAttendeeDTO {
    private UUID userId;
    private String email;
    private String username;
    private String displayName;
    private String avatarUrl;

    public CalendarEventAttendeeDTO(UserEntity user) {
        this.userId = user.getId();
        this.email = user.getEmail();
        this.username = user.getUsername();
        this.displayName = user.getDisplayName();
        this.avatarUrl = user.getAvatarUrl();
    }
}
