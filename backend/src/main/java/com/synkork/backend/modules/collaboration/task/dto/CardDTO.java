package com.synkork.backend.modules.collaboration.task.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

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

    private List<MemberSummaryDTO> assignees;

    private MemberSummaryDTO createdBy;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public CardDTO(CardEntity e) {
        this.id = e.getId();
        this.title = e.getTitle();
        this.description = e.getDescription();
        this.position = e.getPosition();
        this.spaceId = e.getColumn().getSpace().getId();
        this.columnId = e.getColumn().getId();

        this.assignees = e.getAssignees().stream()
                .map(MemberSummaryDTO::new)
                .collect(Collectors.toList());

        if (e.getCreatedBy() != null) {
            this.createdBy = new MemberSummaryDTO(e.getCreatedBy());
        }

        this.createdAt = e.getCreatedAt();
    }
}
