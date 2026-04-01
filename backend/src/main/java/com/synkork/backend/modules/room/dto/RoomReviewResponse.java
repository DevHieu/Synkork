package com.synkork.backend.modules.room.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomReviewResponse {

    private String roomName;
    private String roomAvatar;
    private Long roomMembers;
    private String username;
    private String userDisplayName;
}
