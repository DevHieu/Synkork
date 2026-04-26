package com.synkork.backend.modules.collaboration.task.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ColumnRequest {
    private String name;
    private String spaceId;
}