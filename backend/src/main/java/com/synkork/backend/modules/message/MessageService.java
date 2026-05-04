package com.synkork.backend.modules.message;

import com.synkork.backend.common.dtos.FileUploaded;
import com.synkork.backend.common.utils.FileService;
import com.synkork.backend.modules.message.dto.MessageDTO;
import com.synkork.backend.modules.message.dto.MessagePageDTO;
import com.synkork.backend.modules.message.dto.ReplyPreviewDTO;
import com.synkork.backend.modules.roomMember.RoomMemberEntity;
import com.synkork.backend.modules.roomMember.RoomMemberRepository;
import com.synkork.backend.modules.roomMember.dto.RoomMemberDto;
import com.synkork.backend.modules.space.SpaceEntity;
import com.synkork.backend.modules.space.SpaceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class MessageService {

    @Autowired
    FileService fileService;

    @Autowired
    MessageRepository messageRepository;
    @Autowired
    SpaceRepository spaceRepository;

    @Autowired
    private RoomMemberRepository roomMemberRepository;

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    public MessagePageDTO getMessagesBySpaceId(UUID spaceId, UUID cursor, boolean isUp, int limit) {
        if (cursor == null) {
            // Lần đầu load, không cần direction
            List<MessageDTO> messages = messageRepository.findFirstPage(spaceId, limit + 1);
            boolean hasMore = messages.size() > limit;
            if (hasMore) messages = messages.subList(0, limit);
            UUID beforeCursor = hasMore ? messages.getLast().getId() : null;
            return returnPageDto(messages, beforeCursor, null, hasMore, false);
        }

        if (isUp) {
            // Scroll lên → load older
            List<MessageDTO> messages = messageRepository.findNextPage(spaceId, cursor, limit + 1);
            boolean hasMore = messages.size() > limit;
            if (hasMore) messages = messages.subList(0, limit);
            UUID beforeCursor = hasMore ? messages.getLast().getId() : null;
            return returnPageDto(messages, beforeCursor, null, hasMore, false);

        } else {
            // Scroll xuống → load newer
            List<MessageDTO> messages = messageRepository.findNewerPage(spaceId, cursor, limit + 1);
            Collections.reverse(messages); // Phải reverse lại để danh sách lấy đúng và hiện đúng
            boolean hasMore = messages.size() > limit;
            if (hasMore) messages = messages.subList(0, limit);
            UUID afterCursor = hasMore ? messages.getFirst().getId() : null;
            return returnPageDto(messages, null, afterCursor, false, hasMore);
        }
    }

    public MessageDTO saveMessage(MessageDTO dto, String senderId) {
        System.out.println(dto.getReplyToId());
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

        if (dto.getReplyToId() != null) {
            entity.setReplyTo(messageRepository.getReferenceById(dto.getReplyToId()));
        }

        MessageEntity newMessage = messageRepository.save(entity);
        dto.setId(newMessage.getId());
        dto.setCreatedAt(newMessage.getCreatedAt());
        dto.setUpdatedAt(newMessage.getUpdatedAt());

        RoomMemberDto senderDto = new RoomMemberDto(sender);
        dto.setSender(senderDto);

        if (dto.getReplyToId() != null) {
            messageRepository.findReplyPreviews(List.of(dto.getReplyToId()))
                    .stream()
                    .findFirst()
                    .ifPresent(dto::setReplyTo);
        }

        return dto;
    }

    public void deleteMessage(UUID messageId) {
        MessageEntity message = messageRepository.findById(messageId)
                .orElseThrow(() -> new IllegalArgumentException("Message not found"));

        message.setContent(null);
        message.setPinned(false);
        message.setDeleted(true);
        messageRepository.save(message);

        if (message.getAttachmentPublicId() != null) {
            if (message.getType() == MessageTypeEnum.IMAGE) {
                fileService.deleteFile(message.getAttachmentPublicId(), "image");
            } else if (message.getType() == MessageTypeEnum.FILE) {
                fileService.deleteFile(message.getAttachmentPublicId(), "raw");
            }
        }
    }

    public MessageDTO updateMessage(MessageDTO dto) {
        MessageEntity entity = messageRepository.findById(dto.getId())
                .orElseThrow(() -> new IllegalArgumentException("Message not found"));

        entity.setContent(dto.getContent());
        entity.setEdited(true); // thêm cái này vào là xong

        MessageEntity saved = messageRepository.save(entity);
        dto.setUpdatedAt(saved.getUpdatedAt());
        dto.setEdited(true);

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

        return returnPageDto(pinnedList, nextCursor, null, hasMore, false); // Danh sách pin ko cần làm infinite scroll 2 chiều nên cho before là null luôn
    }

    public MessagePageDTO findAround(UUID spaceUUID, UUID messageUUID, int limit) {

        List<MessageEntity> before = messageRepository.findBeforeMessage(spaceUUID, messageUUID, limit + 1);
        List<MessageEntity> after = messageRepository.findAfterMessage(spaceUUID, messageUUID, limit + 1);

        boolean beforeHasMore = before.size() > limit;
        boolean afterHasMore = after.size() > limit;

        if (beforeHasMore) before = before.subList(0, limit);
        if (afterHasMore) after = after.subList(0, limit);

        Collections.reverse(after);

        // Gộp 2 list lại
        List<MessageEntity> combined = new java.util.ArrayList<>(after);
        combined.addAll(before);

        List<MessageDTO> messages = combined.stream()
                .map(MessageDTO::new)
                .toList();

        UUID beforeCursor = beforeHasMore ? messages.getLast().getId() : null;
        UUID afterCursor = afterHasMore ? messages.getFirst().getId() : null;


        return returnPageDto(messages, beforeCursor, afterCursor, beforeHasMore, afterHasMore);
    }

    // Hàm này nhằm load lên những cái tin nhắn được reply
    private void enrichReplyTo(List<MessageDTO> messages) {
        List<UUID> replyToIds = messages.stream()
                .map(MessageDTO::getReplyToId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        if (replyToIds.isEmpty()) return;

        Map<UUID, ReplyPreviewDTO> previewMap = messageRepository
                .findReplyPreviews(replyToIds)
                .stream()
                .collect(Collectors.toMap(ReplyPreviewDTO::getId, r -> r));

        messages.forEach(m -> {
            if (m.getReplyToId() != null) {
                m.setReplyTo(previewMap.get(m.getReplyToId()));
            }
        });
    }

    private MessagePageDTO returnPageDto(List<MessageDTO> messages, UUID beforeCursor, UUID afterCursor, boolean beforeHasMore, boolean afterHasMore) {
        MessagePageDTO page = new MessagePageDTO(messages, beforeCursor, afterCursor, beforeHasMore, afterHasMore);
        enrichReplyTo(messages);

        return page;
    }


    public void sendFileMessage(UUID spaceId, UUID userId, UUID replyToId, List<MultipartFile> fileList) {
        SpaceEntity space = spaceRepository.findById(spaceId).orElseThrow();
        MessageEntity replyTo = replyToId != null ? messageRepository.findById(replyToId).orElse(null) : null;

        RoomMemberEntity sender = roomMemberRepository
                .findByUserIdAndRoom_IdWithUser(userId, space.getRoom().getId())
                .orElseThrow(() -> new IllegalArgumentException("User is not a member of this room"));

        // Gửi từng file message
        for (MultipartFile file : fileList) {
            boolean isImage = file.getContentType() != null && file.getContentType().startsWith("image/");
            FileUploaded uploaded = isImage
                    ? fileService.uploadImage(file, "message_file")
                    : fileService.uploadFile(file, "message_file");

            MessageEntity fileMessage = new MessageEntity();
            fileMessage.setSpace(space);
            fileMessage.setSender(sender);
            fileMessage.setContent(null);
            fileMessage.setType(isImage ? MessageTypeEnum.IMAGE : MessageTypeEnum.FILE);
            fileMessage.setAttachmentUrl(uploaded.url());
            fileMessage.setAttachmentPublicId(uploaded.publicId());
            fileMessage.setAttachmentResourceType(uploaded.resourceType());
            fileMessage.setAttachmentName(uploaded.originalName());
            fileMessage.setReplyTo(replyTo);

            MessageDTO dto = new MessageDTO(messageRepository.save(fileMessage));

            simpMessagingTemplate.convertAndSend("/topic/space/" + spaceId + "/messages", dto);
        }
    }
}
