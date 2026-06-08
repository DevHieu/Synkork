package com.synkork.backend.modules.room.dto;

import com.synkork.backend.modules.room.RoomEntity;
import com.synkork.backend.modules.user.enums.PlanEnum;

import java.util.UUID;

public record RoomDto(
        UUID id,
        String name,
        String  description,
        String roomAvatar,
        PlanEnum currentPlan) {

    public RoomDto(RoomEntity entity) {
        this(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                entity.getAvatarUrl(),
                entity.getOwner().getCurrentPlan()
        );
    }
}
