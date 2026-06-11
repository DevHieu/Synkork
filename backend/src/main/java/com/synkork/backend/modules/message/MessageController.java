package com.synkork.backend.modules.message;

import com.synkork.backend.common.utils.AuthUtils;
import com.synkork.backend.modules.message.dto.MessageDTO;
import com.synkork.backend.modules.message.dto.MessagePageDTO;
import com.synkork.backend.modules.message.dto.MessageRequest;
import com.synkork.backend.security.UserPrinciple;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/spaces/{spaceId}/messages")
public class MessageController {

  @Autowired
  MessageService messageService;

  @Autowired
  private SimpMessagingTemplate messagingTemplate;

    @GetMapping()
    public ResponseEntity<MessagePageDTO> findMessageBySpaceId(
            @NonNull @PathVariable String spaceId,
            @RequestParam(required = false) String cursor,
            @RequestParam(required = false, defaultValue = "true") Boolean isUp,
            @RequestParam(defaultValue = "20") int limit
    ) {
        UUID spaceUUID = UUID.fromString(spaceId);
        UUID cursorUUID = (cursor != null) ? UUID.fromString(cursor) : null;
        return ResponseEntity.ok(messageService.getMessagesBySpaceId(spaceUUID, cursorUUID, isUp, limit));
    }

    @PostMapping
    public ResponseEntity<MessageDTO> createMessage(@PathVariable String spaceId, @RequestBody MessageRequest request) {
        MessageDTO message = messageService.saveMessage(spaceId, request);

        messagingTemplate.convertAndSend(
                "/topic/space/" + message.getSpaceId() + "/messages",
                message);

        return ResponseEntity.ok(message);
    }

    @PutMapping("/{messageId}")
    public ResponseEntity<MessageDTO> updateMessage(@PathVariable String spaceId, @PathVariable String messageId, @RequestBody MessageRequest request) {
        MessageDTO newMessage = messageService.updateMessage(messageId, request);

        messagingTemplate.convertAndSend(
                "/topic/space/" + spaceId + "/messages/update",
                newMessage
        );

        return ResponseEntity.ok(newMessage);
    }

    @DeleteMapping("/{messageId}")
    public ResponseEntity<Map<String, String>> deleteMessage(@PathVariable String spaceId, @PathVariable String messageId) {
        messageService.deleteMessage(UUID.fromString(messageId));

        messagingTemplate.convertAndSend(
                "/topic/space/" + spaceId + "/messages/delete",
                messageId
        );

        return ResponseEntity.ok(
                Map.of("message", "Delete message successfully")
        );
    }

    @GetMapping("/pin")
    public ResponseEntity<MessagePageDTO> findMessagePinnedBySpaceId(@NonNull @PathVariable String spaceId,
                                                                     @RequestParam(required = false) String cursor,
                                                                     @RequestParam(defaultValue = "20") int limit) {
        UUID spaceUUID = UUID.fromString(spaceId);
        UUID cursorUUID = (cursor != null) ? UUID.fromString(cursor) : null;
        return ResponseEntity.ok(messageService.getMessagesPinnedBySpaceId(spaceUUID, cursorUUID, limit));
    }

    @PutMapping("/pin/{messageId}")
    public ResponseEntity<MessageDTO> changeMessagePinStatus(@PathVariable String spaceId, @PathVariable String messageId) {
        UUID messageUUID = UUID.fromString(messageId);

        MessageDTO message = messageService.changeMessagePinStatus(messageUUID);
        messagingTemplate.convertAndSend("/topic/space/" + spaceId + "/messages/pin", message);

        return ResponseEntity.ok(message);
    }

    @GetMapping("/around/{messageId}")
    public ResponseEntity<MessagePageDTO> findMessageAround(@PathVariable String spaceId, @PathVariable String messageId, @RequestParam(defaultValue = "20") int limit) {
        UUID spaceUUID = UUID.fromString(spaceId);
        UUID messageUUID = UUID.fromString(messageId);

        MessagePageDTO messages = messageService.findAround(spaceUUID, messageUUID, limit);

        return  ResponseEntity.ok(messages);
    }

    @GetMapping("/search")
    public ResponseEntity<MessagePageDTO> searchMessages(
            @PathVariable String spaceId,
            @RequestParam String keyword,
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "5") int limit
    ) {
        UUID spaceUUID = UUID.fromString(spaceId);
        UUID cursorUUID = cursor != null ? UUID.fromString(cursor) : null;
        return ResponseEntity.ok(messageService.searchMessages(spaceUUID, keyword, cursorUUID, limit));
    }

    @PostMapping("/file")
    public ResponseEntity<?> createMessageFile(
            @RequestParam List<MultipartFile> fileList,
            @RequestParam(required = false) String replyToId,
            @PathVariable String spaceId
    ) {
        UserPrinciple userPrinciple = (UserPrinciple) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        UUID spaceUUID = UUID.fromString(spaceId);
        UUID replyToUUID = replyToId != null ? UUID.fromString(replyToId) : null;

        messageService.sendFileMessage(spaceUUID, userPrinciple.getId(), replyToUUID, fileList);
        // Broadcast file trong service luôn

        return ResponseEntity.ok().build();
    }


}
