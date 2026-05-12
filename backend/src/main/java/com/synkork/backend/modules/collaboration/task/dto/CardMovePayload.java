package com.synkork.backend.modules.collaboration.task.dto;

import java.util.List;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;

// CardMovePayload.java - chỉ cần 1 record đơn giản
@Data
@AllArgsConstructor
public class CardMovePayload {
    private UUID targetColumnId;
    private UUID sourceColumnId;          // null nếu same-column
    private List<CardDTO> targetCards;    // toàn bộ cards của cột đích
    private List<CardDTO> sourceCards;    // toàn bộ cards của cột nguồn, null nếu same-column
}
