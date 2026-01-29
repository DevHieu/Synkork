package com.synkork.backend.modules.message;

import com.synkork.backend.modules.message.dto.MessageDTO;
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

  @PostMapping
  public ResponseEntity<MessageEntity> createMessage(@NonNull @RequestBody MessageEntity entity) {

    return messageService.createMessage(entity)
        .map(savedMessage -> ResponseEntity.ok(savedMessage))
        .orElseGet(() -> ResponseEntity.badRequest().body(null));
  }

  @GetMapping("/{spaceId}")
  public ResponseEntity<Page<MessageDTO>> findMessageBySpaceId(@NonNull @PathVariable("spaceId") UUID spaceId,@RequestParam(defaultValue = "0") int page,
                                                               @RequestParam(defaultValue = "20") int size) {
      if (page < 1) {
          page = 1;
      }

      Pageable pageable = PageRequest.of(page-1, size, Sort.by("createdAt").descending());
      return ResponseEntity.ok(messageService.getMessagesBySpaceId(spaceId, pageable));
  }

}
