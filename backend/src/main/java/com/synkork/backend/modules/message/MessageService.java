package com.synkork.backend.modules.message;

import com.synkork.backend.modules.message.dto.MessageDTO;
import com.synkork.backend.modules.message.dto.MessagePageDTO;
import com.synkork.backend.modules.roomMember.RoomMemberEntity;
import com.synkork.backend.modules.roomMember.RoomMemberRepository;
import com.synkork.backend.modules.roomMember.dto.RoomMemberDto;
import com.synkork.backend.modules.space.SpaceEntity;
import com.synkork.backend.modules.space.SpaceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class MessageService {
    @Autowired
    MessageRepository messageRepository;

    @Autowired
    SpaceRepository spaceRepository;

    @Autowired
    private RoomMemberRepository roomMemberRepository;

    public MessagePageDTO getMessagesBySpaceId(UUID spaceId, UUID cursor, int limit) {
        List<MessageDTO> messages = (cursor == null)
                ? messageRepository.findFirstPage(spaceId, limit + 1)
                : messageRepository.findNextPage(spaceId, cursor, limit + 1);

        boolean hasMore = messages.size() > limit;
        if (hasMore) {
            messages = messages.subList(0, limit);
        }

        UUID nextCursor = hasMore ? messages.get(messages.size() - 1).getId() : null;

        return new MessagePageDTO(messages, nextCursor, hasMore);
    }

    public MessageDTO saveMessage(MessageDTO dto, String senderId) {
        MessageEntity entity = new MessageEntity();
        UUID userId = UUID.fromString(senderId);
        UUID spaceId = UUID.fromString(dto.getSpaceId());

        SpaceEntity space = spaceRepository.findById(spaceId)
                .orElseThrow(() -> new IllegalArgumentException("Space not found"));

        RoomMemberEntity sender = roomMemberRepository
                .findByUserIdAndRoom_IdWithUser(userId, space.getRoom().getId())
                .orElseThrow(() -> new IllegalArgumentException("User is not a member of this room"));

        entity.setSender(sender);
        entity.setSpace(space);
        entity.setContent(dto.getContent());

        MessageEntity newMessage = messageRepository.save(entity);
        System.out.println("createdAt after save: " + newMessage.getCreatedAt());
        dto.setId(newMessage.getId());
        dto.setCreatedAt(newMessage.getCreatedAt());
        dto.setUpdatedAt(newMessage.getUpdatedAt());

        RoomMemberDto senderDto = new RoomMemberDto(sender);
        dto.setSender(senderDto);

        return dto;
    }

    public void deleteMessage(UUID messageId) {
        MessageEntity message = messageRepository.findById(messageId)
                .orElseThrow(() -> new IllegalArgumentException("Message not found"));

        message.setDeleted(true);
        messageRepository.save(message);
    }

    public MessageDTO updateMessage(MessageDTO dto) {
        MessageEntity  entity = messageRepository.findById(dto.getId()).orElseThrow(() -> new IllegalArgumentException("Message not found"));
        entity.setContent(dto.getContent());
        MessageEntity newMessage = messageRepository.save(entity);

        dto.setUpdatedAt(newMessage.getUpdatedAt());

        return dto;
    }
}
