package com.synkork.backend.modules.space.dto;

import com.synkork.backend.modules.space.SpaceEntity;
import com.synkork.backend.modules.space.enums.SpaceTypeEnum;

import java.util.UUID;

public record SpaceDTO(UUID id, String name, SpaceTypeEnum type, boolean restricted) {
    public SpaceDTO(SpaceEntity space) {
        this(
                space.getId(),
                space.getName(),
                space.getType(),
                space.isRestricted()
        );
    }
}
