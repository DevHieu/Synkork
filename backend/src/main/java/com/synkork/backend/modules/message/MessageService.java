package com.synkork.backend.modules.message;

import com.synkork.backend.modules.message.dto.MessageDTO;
import com.synkork.backend.modules.message.dto.MessagePageDTO;
import com.synkork.backend.modules.roomMember.RoomMemberEntity;
import com.synkork.backend.modules.roomMember.RoomMemberRepository;
import com.synkork.backend.modules.roomMember.dto.RoomMemberDto;
import com.synkork.backend.modules.space.SpaceEntity;
import com.synkork.backend.modules.space.SpaceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class MessageService {
    @Autowired
    MessageRepository messageRepository;

    @Autowired
    SpaceRepository spaceRepository;

    @Autowired
    private RoomMemberRepository roomMemberRepository;

    public MessagePageDTO getMessagesBySpaceId(UUID spaceId, UUID cursor, boolean isUp, int limit) {
        if (cursor == null) {
            // Lần đầu load, không cần direction
            List<MessageDTO> messages = messageRepository.findFirstPage(spaceId, limit + 1);
            boolean hasMore = messages.size() > limit;
            if (hasMore) messages = messages.subList(0, limit);
            UUID beforeCursor = hasMore ? messages.getLast().getId() : null;
            return new MessagePageDTO(messages, beforeCursor, null, hasMore, false);
        }

        if (isUp) {
            // Scroll lên → load older
            List<MessageDTO> messages = messageRepository.findNextPage(spaceId, cursor, limit + 1);
            boolean hasMore = messages.size() > limit;
            if (hasMore) messages = messages.subList(0, limit);
            UUID beforeCursor = hasMore ? messages.getLast().getId() : null;
            return new MessagePageDTO(messages, beforeCursor, null, hasMore, false);

        } else {
            // Scroll xuống → load newer
            List<MessageDTO> messages = messageRepository.findNewerPage(spaceId, cursor, limit + 1);
            boolean hasMore = messages.size() > limit;
            if (hasMore) messages = messages.subList(0, limit);
            UUID afterCursor = hasMore ? messages.getLast().getId() : null;
            return new MessagePageDTO(messages, null, afterCursor, false, hasMore);
        }
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

    public MessageDTO changeMessagePinStatus(UUID messageUUID) {
        MessageEntity entity = messageRepository.findById(messageUUID).orElseThrow(() -> new IllegalArgumentException("Message not found"));

        entity.setPinned(!entity.isPinned());
        messageRepository.save(entity);

        return new MessageDTO(entity);
    }

    public MessagePageDTO getMessagesPinnedBySpaceId(UUID spaceUUID, UUID cursorUUID, int limit) {
        List<MessageDTO> pinnedList = cursorUUID == null ?
                messageRepository.findPinnedFirstPage(spaceUUID, limit + 1) :
                messageRepository.findPinnedNextPage(spaceUUID, cursorUUID, limit + 1);

        boolean hasMore = pinnedList.size() > limit;
        if (hasMore) {
            pinnedList = pinnedList.subList(0, limit);
        }

        UUID nextCursor = hasMore ? pinnedList.get(pinnedList.size() - 1).getId() : null;

        return new MessagePageDTO(pinnedList, nextCursor, null, hasMore, false); // Danh sách pin ko cần làm infinite scroll 2 chiều nên cho before là null luôn
    }

    public MessagePageDTO findAround(UUID spaceUUID, UUID messageUUID, int limit) {

        List<MessageEntity> before = messageRepository.findBeforeMessage(spaceUUID, messageUUID, limit + 1);
        List<MessageEntity> after = messageRepository.findAfterMessage(spaceUUID, messageUUID, limit + 1);

        boolean beforeHasMore = before.size() > limit;
        boolean afterHasMore = after.size() > limit;

        if (beforeHasMore) before = before.subList(0, limit);
        if (afterHasMore) after = after.subList(0, limit);

        Collections.reverse(before);

        // Gộp 2 list lại
        List<MessageEntity> combined = new java.util.ArrayList<>(before);
        combined.addAll(after);

        List<MessageDTO> messages = combined.stream()
                .map(MessageDTO::new)
                .toList();

        UUID beforeCursor = beforeHasMore ? messages.getFirst().getId() : null;
        UUID afterCursor = afterHasMore ? messages.getLast().getId() : null;

        return new MessagePageDTO(messages, beforeCursor, afterCursor, beforeHasMore, afterHasMore);
    }
}
