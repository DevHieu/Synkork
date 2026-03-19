package com.synkork.backend.modules.user.dto;

import com.synkork.backend.modules.message.MessageTypeEnum;
import com.synkork.backend.modules.user.enums.RoleEnum;

public record SenderDto(String username, String displayName, String avatarUrl, RoleEnum role) {
}
