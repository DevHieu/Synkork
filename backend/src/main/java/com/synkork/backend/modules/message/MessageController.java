package com.synkork.backend.modules.message;

import com.synkork.backend.modules.message.dto.MessageDTO;
import com.synkork.backend.modules.message.dto.MessagePageDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;

import java.util.UUID;

@RestController
@RequestMapping("/messages")
public class MessageController {

  @Autowired
  MessageService messageService;

    @GetMapping("/{spaceId}")
    public ResponseEntity<MessagePageDTO> findMessageBySpaceId(
            @NonNull @PathVariable String spaceId,
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "20") int size
    ) {
        UUID spaceUUID = UUID.fromString(spaceId);
        UUID cursorUUID = (cursor != null) ? UUID.fromString(cursor) : null;
        return ResponseEntity.ok(messageService.getMessagesBySpaceId(spaceUUID, cursorUUID, size));
    }

}
