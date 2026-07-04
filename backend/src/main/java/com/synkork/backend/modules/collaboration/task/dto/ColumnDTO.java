package com.synkork.backend.modules.collaboration.task.dto;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.synkork.backend.modules.collaboration.task.card.CardEntity;
import com.synkork.backend.modules.collaboration.task.column.ColumnEntity;

import lombok.Data;

@Data
public class ColumnDTO {
    private UUID id;
    private String name;
    private int position;
    private UUID spaceId;
    private List<CardDTO> cards;
    private Boolean archived;
    private LocalDateTime archivedAt;

    public ColumnDTO(ColumnEntity e){
        this.id = e.getId();
        this.name = e.getName();    
        this.position = e.getPosition();
        this.spaceId = e.getSpace().getId();
        this.cards = e.getCards().stream()
            .filter(card ->
        !Boolean.TRUE.equals(card.getArchived())
    )
            .sorted(Comparator.comparingInt(CardEntity::getPosition))
            .map(CardDTO::new)
            .collect(Collectors.toList());
        this.archived = e.getArchived();
        this.archivedAt = e.getArchivedAt();
    }
}