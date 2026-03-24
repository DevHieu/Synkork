package com.synkork.backend.modules.message.dto;

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

    private MessageTypeEnum type = MessageTypeEnum.TEXT;

    private String attachmentUrl;

    private RoomMemberDto sender;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public MessageDTO(UUID id,String content, UUID spaceId, boolean deleted, boolean pinned,
                      MessageTypeEnum type, String attachmentUrl,
                      String senderUsername, String senderDisplayName,
                      String senderAvatarUrl, RoomMemberRoleEnum senderRole,
                      LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.content = content;
        this.spaceId = spaceId.toString();
        this.deleted = deleted;
        this.pinned = pinned;
        this.type = type;
        this.attachmentUrl = attachmentUrl;
        this.sender = new RoomMemberDto(senderDisplayName, senderUsername, senderAvatarUrl, senderRole);
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}
