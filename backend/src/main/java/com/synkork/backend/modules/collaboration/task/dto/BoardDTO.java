package com.synkork.backend.modules.collaboration.task.dto;

import java.util.List;
import java.util.UUID;

import lombok.Data;

@Data
public class BoardDTO {
    private UUID id;
    private String name;
    private List<ColumnDTO> columns;
}
