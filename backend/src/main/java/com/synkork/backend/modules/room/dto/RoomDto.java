package com.synkork.backend.modules.room.dto;

import com.synkork.backend.modules.room.RoomEntity;

import java.util.UUID;

public record RoomDto(
        UUID id,
        String name,
        String  description,
        String roomAvatar) {

    public RoomDto(RoomEntity entity) {
        this(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                entity.getAvatarUrl()
        );
    }
}
