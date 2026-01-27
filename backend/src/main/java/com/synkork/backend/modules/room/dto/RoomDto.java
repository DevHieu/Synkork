package com.synkork.backend.modules.room.dto;

import java.util.UUID;

public record RoomDto(
        UUID id,
        String name,
        String roomAvatar,
        UUID ownerId) {
}
