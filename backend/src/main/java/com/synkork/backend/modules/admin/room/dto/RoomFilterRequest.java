package com.synkork.backend.modules.admin.room.dto;

import lombok.Data;

@Data
public class RoomFilterRequest {
    private String search;
    private int page = 0;
    private int size = 20;
}