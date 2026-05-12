package com.synkork.backend.modules.collaboration.task.dto;

import java.util.List;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CardRequest {
    private UUID columnId;
    private String title;
    private String description;
    private UUID userId;

    private List<UUID> assigneeIds;
}
