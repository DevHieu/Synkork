package com.synkork.backend.modules.message.dto;

import com.synkork.backend.modules.message.MessageEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReplyPreviewDTO {
    private UUID id;
    private String content;
    private boolean deleted;
    private String senderDisplayName;

    public ReplyPreviewDTO(MessageEntity message) {
        this.id = message.getId();
        this.content = message.getContent();
        this.deleted = message.isDeleted();
        this.senderDisplayName = message.getSender().getUser().getDisplayName();
    }
}