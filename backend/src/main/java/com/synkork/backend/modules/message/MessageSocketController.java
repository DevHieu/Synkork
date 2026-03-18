package com.synkork.backend.modules.message;

import com.synkork.backend.modules.message.dto.MessageDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class MessageSocketController {
  @Autowired
  private SimpMessagingTemplate messagingTemplate;

  @Autowired
  MessageService messageService;

  @MessageMapping("/chat.sendMessage")
  public void sendMessage(@Payload MessageDTO dto, SimpMessageHeaderAccessor headerAccessor) {

      String senderId = (String) headerAccessor
              .getSessionAttributes()
              .get("userId");

      MessageDTO message = messageService.saveMessage(dto, senderId);

    messagingTemplate.convertAndSend(
        "/topic/space/" + message.getSpaceId() + "/messages",
            message);
  }
}
