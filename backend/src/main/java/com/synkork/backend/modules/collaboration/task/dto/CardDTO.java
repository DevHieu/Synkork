package com.synkork.backend.modules.collaboration.task.dto;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

import com.synkork.backend.modules.collaboration.task.card.CardEntity;

import lombok.Data;

@Data
public class CardDTO {
    private UUID id;
    private String title;
    private String description;
    private int position;

    private UUID spaceId;
    private UUID columnId;

    private UUID assigneeId;
    
    private Set<UUID> assigneeIds;

    private UUID createdById;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public CardDTO(CardEntity e) {
        this.id = e.getId();
        this.title = e.getTitle();
        this.description = e.getDescription();
        this.position = e.getPosition();
        this.spaceId = e.getColumn().getSpace().getId();
        this.columnId = e.getColumn().getId();
        if (e.getAssignee() != null) {
            this.assigneeId = e.getAssignee().getId();
        }
    }
}
