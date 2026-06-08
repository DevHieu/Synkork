package com.synkork.backend.modules.admin.room.dto;

import lombok.Data;

@Data
public class AdminRoomRequest {
    private String name;
    private String description;
    private String status;   // "OPEN" / "CLOSED"
    private String ownerId;  // UUID của user làm owner
}