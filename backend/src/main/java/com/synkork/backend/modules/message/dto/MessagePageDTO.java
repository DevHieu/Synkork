package com.synkork.backend.modules.message.dto;

import java.util.List;
import java.util.UUID;

public record MessagePageDTO(List<MessageDTO> messages,
                             UUID beforeCursor,   // id của message trên cùng
                             UUID afterCursor,   // id của message dưới cùng
                             boolean beforeHasMore,
                             boolean afterHasMore) {
}
