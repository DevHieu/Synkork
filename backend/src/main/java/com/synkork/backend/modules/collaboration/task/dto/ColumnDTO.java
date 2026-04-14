package com.synkork.backend.modules.collaboration.task.dto;

import java.util.List;
import java.util.UUID;

import com.synkork.backend.modules.collaboration.task.column.ColumnEntity;

import lombok.Data;

@Data
public class ColumnDTO {
    private UUID id;
    private String name;
    private int position;
    private UUID boardId;
    private List<CardDTO> cards;

    public ColumnDTO(ColumnEntity e){
        this.id = e.getId();
        this.name = e.getName();    
        this.position = e.getPosition();
        this.boardId = e.getBoard().getId();
    }
}