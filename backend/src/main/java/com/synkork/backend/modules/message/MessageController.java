package com.synkork.backend.modules.message;

import org.springframework.web.bind.annotation.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;

@RestController
@RequestMapping("/messages")
public class MessageController {

  @Autowired
  MessageService messageService;

  @PostMapping("/")
  public ResponseEntity<MessageEntity> createMessage(@NonNull @RequestBody MessageEntity entity) {

    return messageService.createMessage(entity)
        .map(savedMessage -> ResponseEntity.ok(savedMessage))
        .orElseGet(() -> ResponseEntity.badRequest().body(null));
  }

}
