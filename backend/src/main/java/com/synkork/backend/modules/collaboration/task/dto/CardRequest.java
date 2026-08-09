package com.synkork.backend.modules.collaboration.task.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonFormat;

public record CardRequest(
        UUID columnId,
        String title,
        String description,
        UUID userId,
        List<UUID> assigneeIds,

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime dueDate,

        Integer version,

        Boolean completed
) {
}
