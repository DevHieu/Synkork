package com.synkork.backend.modules.collaboration.task.dto;

import java.util.UUID;

import lombok.Data;

@Data
public class MoveCardRequest {
    private UUID targetColumnId;
    private int newPosition;
    private Integer version;
}
