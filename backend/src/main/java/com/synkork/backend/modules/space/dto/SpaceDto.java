package com.synkork.backend.modules.space.dto;

import com.synkork.backend.modules.room.RoomEntity;
import com.synkork.backend.modules.space.SpaceEntity;
import com.synkork.backend.modules.space.SpaceTypeEnum;

import java.util.UUID;

public record SpaceDto(UUID id, String name, SpaceTypeEnum type) {
}
