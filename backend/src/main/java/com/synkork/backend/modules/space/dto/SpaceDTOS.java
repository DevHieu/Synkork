package com.synkork.backend.modules.space.dto;

import com.synkork.backend.modules.space.SpaceEntity;
import com.synkork.backend.modules.space.enums.SpaceTypeEnum;

import java.util.UUID;

public record SpaceDTOS(UUID id, String name, SpaceTypeEnum type, boolean restricted) {
    public SpaceDTOS(SpaceEntity space) {
        this(
                space.getId(),
                space.getName(),
                space.getType(),
                space.isRestricted()
        );
    }
}
