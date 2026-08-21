package com.synkork.backend.modules.collaboration.task.dto;

public record CompleteCardRequest(
        boolean completed,
        Integer version
) {
}
