package com.synkork.backend.modules.message.dto;

import com.synkork.backend.modules.message.MessageEntity;
import com.synkork.backend.modules.message.MessageTypeEnum;
import com.synkork.backend.modules.roomMember.dto.RoomMemberDto;
import com.synkork.backend.modules.roomMember.enums.RoomMemberRoleEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageDTO {
    private UUID id;
    private String content;
    private String spaceId;

    private boolean deleted = false;
    private boolean pinned = false;
    private boolean edited = false;

    private MessageTypeEnum type = MessageTypeEnum.TEXT;

    private String attachmentUrl;

    private RoomMemberDto sender;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public MessageDTO (MessageEntity message) {
        this.id = message.getId();
        this.content = message.getContent();
        this.spaceId = message.getSpace().getId().toString();
        this.deleted = message.isDeleted();
        this.pinned = message.isPinned();
        this.edited = message.isEdited();
        this.createdAt = message.getCreatedAt();
        this.updatedAt = message.getUpdatedAt();
        this.type = message.getType();
        this.attachmentUrl = message.getAttachmentUrl();
        this.sender = new RoomMemberDto(
                message.getSender().getUser().getDisplayName(),
                message.getSender().getUser().getUsername(),
                message.getSender().getUser().getAvatarUrl(),
                message.getSender().getRole()
        );

    }

    public MessageDTO(UUID id,String content, UUID spaceId, boolean deleted, boolean pinned, boolean edited,
                      MessageTypeEnum type, String attachmentUrl,
                      String senderUsername, String senderDisplayName,
                      String senderAvatarUrl, RoomMemberRoleEnum senderRole,
                      LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.content = content;
        this.spaceId = spaceId.toString();
        this.deleted = deleted;
        this.pinned = pinned;
        this.edited = edited;
        this.type = type;
        this.attachmentUrl = attachmentUrl;
        this.sender = new RoomMemberDto(senderDisplayName, senderUsername, senderAvatarUrl, senderRole);
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}
