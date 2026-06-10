package com.synkork.backend.modules.admin.workspace.spaces.dtos;

import com.synkork.backend.modules.space.SpaceEntity;
import com.synkork.backend.modules.space.enums.SpaceStatusEnum;
import com.synkork.backend.modules.space.enums.SpaceTypeEnum;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class AdminRoomSpaceResponse {
    private UUID id;
    private String name;
    private SpaceTypeEnum type;
    private SpaceStatusEnum status;
    private boolean isRestricted;
    private LocalDateTime createdAt;

    public AdminRoomSpaceResponse(SpaceEntity space) {
        this.id = space.getId();
        this.name = space.getName();
        this.type = space.getType();
        this.status = space.getStatus();
        this.isRestricted = space.isRestricted();
        this.createdAt = space.getCreatedAt();
    }
}