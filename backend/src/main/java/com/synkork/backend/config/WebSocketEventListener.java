package com.synkork.backend.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

// Lớp này lắng nghe các sự kiện kết nối và ngắt kết nối WebSocket (join out room)
@Component
public class WebSocketEventListener {
  private static final Logger logger = LoggerFactory.getLogger(WebSocketEventListener.class);

  @EventListener
  public void handleWebSocketConnectListener(SessionConnectedEvent event) {
    logger.info("Received a new web socket connection");
  }

  @EventListener
  public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
    StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
    logger.info("Someone disconnect");
    if (headerAccessor.getSessionAttributes() != null) {
      String username = (String) headerAccessor.getSessionAttributes().get("username");
      if (username != null) {
        logger.info("User Disconnected : " + username);
      }
    }
  }
}
