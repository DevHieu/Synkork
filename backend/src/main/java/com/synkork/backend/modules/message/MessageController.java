package com.synkork.backend.modules.message;

import com.synkork.backend.modules.message.dto.MessageDTO;
import com.synkork.backend.modules.message.dto.MessagePageDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;

import java.util.List;
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
}
