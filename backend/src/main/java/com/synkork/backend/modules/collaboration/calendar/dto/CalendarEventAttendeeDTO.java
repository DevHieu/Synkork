package com.synkork.backend.modules.collaboration.calendar.dto;

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
}
