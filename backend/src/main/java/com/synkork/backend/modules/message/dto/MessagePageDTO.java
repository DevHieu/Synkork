package com.synkork.backend.modules.message.dto;

import java.util.List;
import java.util.UUID;

public record MessagePageDTO(List<MessageDTO> messages,
                             UUID nextCursor,   // id của message cuối cùng
                             boolean hasMore) { }
