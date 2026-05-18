package com.synkork.backend.modules.message;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.google.api.client.json.Json;
import com.synkork.backend.common.dtos.FileUploaded;
import com.synkork.backend.common.utils.FileService;
import com.synkork.backend.common.utils.llmService;
import com.synkork.backend.modules.message.dto.MessageDTO;
import com.synkork.backend.modules.message.dto.MessagePageDTO;
import com.synkork.backend.modules.message.dto.MessageSuggestionDTO;
import com.synkork.backend.modules.message.dto.ReplyPreviewDTO;
import com.synkork.backend.modules.roomMember.RoomMemberEntity;
import com.synkork.backend.modules.roomMember.RoomMemberRepository;
import com.synkork.backend.modules.roomMember.dto.RoomMemberDto;
import com.synkork.backend.modules.space.SpaceEntity;
import com.synkork.backend.modules.space.SpaceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;


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
    private llmService LLMService;
    @Autowired
    private ObjectMapper objectMapper;
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

    @Transactional
    public MessageDTO saveMessage(MessageDTO dto, String senderId, String senderEmail) {
        System.out.println("[Tin nhan] Bat dau luu tin nhan. spaceId=" + dto.getSpaceId()
                + ", senderId=" + senderId
                + ", senderEmail=" + senderEmail
                + ", replyToId=" + dto.getReplyToId());
        MessageEntity entity = new MessageEntity();
        UUID spaceId = UUID.fromString(dto.getSpaceId());

        SpaceEntity space = spaceRepository.findById(spaceId)
                .orElseThrow(() -> new IllegalArgumentException("Space not found"));

        RoomMemberEntity sender = resolveSender(space.getRoom().getId(), senderId, senderEmail);

        entity.setSender(sender);
        entity.setSpace(space);
        entity.setContent(dto.getContent());

        if (dto.getReplyToId() != null) {
            entity.setReplyTo(messageRepository.getReferenceById(dto.getReplyToId()));
        }

        MessageEntity newMessage = messageRepository.saveAndFlush(entity);
        System.out.println("[Tin nhan] Da luu tin nhan. messageId=" + newMessage.getId()
                + ", roomId=" + space.getRoom().getId()
                + ", senderMemberId=" + sender.getId());

        // Dựng lại DTO từ entity vừa lưu để đảm bảo createdAt / updatedAt / sender luôn đầy đủ.
        MessageDTO responseDto = new MessageDTO(newMessage);
        responseDto.setReplyToId(dto.getReplyToId());

        // LLM 100% AI Code for AI Sug (")>
        String messageContent = responseDto.getContent();

        if (messageContent != null && !messageContent.trim().isEmpty()) {
            CompletableFuture.runAsync(()->{
                try {
                    String jsonRepsone = LLMService.detectEventFromMessage(messageContent);
                    System.out.println("[Goi y LLM] Phan hoi tho cho message " + newMessage.getId() + ": " + jsonRepsone);

                    JsonNode rootNode = objectMapper.readTree(jsonRepsone);
                          
                    // 
                    if (rootNode.has("hasEvent") && rootNode.get("hasEvent").asBoolean()) {
                        // Bọc thêm messageId để frontend biết gợi ý này thuộc tin nhắn nào.
                        MessageSuggestionDTO suggestionPayload = MessageSuggestionDTO.fromJsonNode(
                                newMessage.getId(),
                                rootNode
                        );
                        // Luôn dùng userId thật từ sender đã resolve để tránh lệch với id trong websocket session.
                        String privateChannel = "/topic/user/" + sender.getUser().getId() + "/suggestions";
                        System.out.println("[Goi y LLM] Dang gui toi " + privateChannel
                                + " for messageId=" + newMessage.getId()
                                + " payload=" + suggestionPayload);
                        simpMessagingTemplate.convertAndSend(privateChannel, suggestionPayload);
                    } else {
                        System.out.println("[Goi y LLM] Bo qua message " + newMessage.getId() + " vi hasEvent=false");
                    }

                } catch (JsonMappingException e) {
                    System.err.println("Loi khi phan tich tin nhan bang LLM: " + e.getMessage());
                } catch (JsonProcessingException e) {
                    System.err.println("Loi khi phan tich tin nhan bang LLM: " + e.getMessage());
                }  catch (Exception e) {
                    System.err.println("Loi khi phan tich tin nhan bang LLM: " + e.getMessage());
                }
            });
        }

        return responseDto;
    }

    private RoomMemberEntity resolveSender(UUID roomId, String senderId, String senderEmail) {
        // Ưu tiên dùng userId từ websocket session vì đây là định danh ổn định nhất.
        if (senderId != null && !senderId.isBlank()) {
            try {
                UUID userId = UUID.fromString(senderId);
                Optional<RoomMemberEntity> senderById = roomMemberRepository
                        .findByUserIdAndRoom_IdWithUser(userId, roomId);
                if (senderById.isPresent()) {
                    System.out.println("[Tin nhan] Tim duoc sender theo userId=" + senderId + " trong roomId=" + roomId);
                    return senderById.get();
                }
                System.out.println("[Tin nhan] Khong tim thay sender theo userId=" + senderId + " trong roomId=" + roomId);
            } catch (IllegalArgumentException ignored) {
                // Bỏ qua để fallback sang email nếu userId trong session không hợp lệ.
                System.out.println("[Tin nhan] senderId khong phai UUID hop le: " + senderId);
            }
        }

        // Fallback theo email để tránh lỗi nếu claim userId trong websocket session bị lệch.
        if (senderEmail != null && !senderEmail.isBlank()) {
            Optional<RoomMemberEntity> senderByEmail = roomMemberRepository
                    .findByUser_EmailAndRoom_Id(senderEmail, roomId);
            if (senderByEmail.isPresent()) {
                System.out.println("[Tin nhan] Tim duoc sender theo email=" + senderEmail + " trong roomId=" + roomId);
                return senderByEmail.get();
            }
            System.out.println("[Tin nhan] Khong tim thay sender theo email=" + senderEmail + " trong roomId=" + roomId);
        }

        System.out.println("[Tin nhan] Khong the xac dinh sender. roomId=" + roomId
                + ", senderId=" + senderId
                + ", senderEmail=" + senderEmail);
        throw new IllegalArgumentException("User is not a member of this room");
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

    public MessagePageDTO searchMessages(UUID spaceId, String keyword, UUID cursor, int limit) {
        List<MessageDTO> messages = cursor == null
                ? messageRepository.searchFirstPage(spaceId, keyword, limit + 1)
                : messageRepository.searchNextPage(spaceId, keyword, cursor, limit + 1);

        boolean hasMore = messages.size() > limit;
        if (hasMore) messages = messages.subList(0, limit);
        UUID beforeCursor = hasMore ? messages.getLast().getId() : null;

        return returnPageDto(messages, beforeCursor, null, hasMore, false);
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
