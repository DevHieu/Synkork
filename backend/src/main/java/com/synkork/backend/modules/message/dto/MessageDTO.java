package com.synkork.backend.modules.message.dto;

import com.synkork.backend.modules.message.MessageTypeEnum;
import com.synkork.backend.modules.space.SpaceEntity;
import com.synkork.backend.modules.user.UserEntity;
import com.synkork.backend.modules.user.dto.SenderDto;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageDTO {
  private String content;
  private String spaceId;

    private boolean deleted = false;
    private boolean pinned = false;

    private MessageTypeEnum type =  MessageTypeEnum.TEXT;

    private String attachmentUrl;

    private SenderDto sender;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public MessageDTO(MessageProjection p) {
        this.content = p.getContent();
        this.deleted = p.getDeleted();
        this.pinned = p.getPinned();
        this.type = p.getType();
        this.attachmentUrl = p.getAttachmentUrl();
        this.createdAt = p.getCreatedAt();
        this.updatedAt = p.getUpdatedAt();
        this.sender = new SenderDto(
                p.getSender().getUsername(),
                p.getSender().getDisplayName(),
                p.getSender().getAvatarUrl(),
                p.getSender().getRole()
        );
    }
}
