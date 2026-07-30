package com.synkork.backend.modules.collaboration.task.dto;

public record ColumnRequest(
        String name,
        String spaceId,
        Integer version
) {
}