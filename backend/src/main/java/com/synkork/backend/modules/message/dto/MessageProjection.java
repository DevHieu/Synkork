package com.synkork.backend.modules.message.dto;

import com.synkork.backend.modules.message.MessageTypeEnum;
import com.synkork.backend.modules.user.enums.RoleEnum;

import java.time.LocalDateTime;
import java.util.UUID;

public interface MessageProjection {
    String getContent();
    Boolean getDeleted();
    Boolean getPinned();
    MessageTypeEnum getType();
    String getAttachmentUrl();
    LocalDateTime getCreatedAt();
    LocalDateTime getUpdatedAt();

    SenderDTO getSender();

    interface SenderDTO {
        UUID getId();
        String getUsername();
        String getDisplayName();
        String getAvatarUrl();
        RoleEnum getRole();
    }
}
