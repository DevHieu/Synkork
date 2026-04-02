package com.synkork.backend.modules.space.dto;

import com.synkork.backend.modules.space.enums.SpaceTypeEnum;

import java.util.UUID;

public record SpaceDto(UUID id, String name, SpaceTypeEnum type) {
}
