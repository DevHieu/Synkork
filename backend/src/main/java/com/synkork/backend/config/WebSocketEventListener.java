package com.synkork.backend.config;

import com.synkork.backend.modules.friend.FriendService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Lớp này lắng nghe các sự kiện kết nối và ngắt kết nối WebSocket (join out room)
@Component
public class WebSocketEventListener {
    private static final Logger logger = LoggerFactory.getLogger(WebSocketEventListener.class);

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    @Autowired
    private FriendService friendService;

    // Lưu lại các user online
    private final Map<String, String> onlineUsers = new HashMap<>();

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectEvent event) {
        logger.info("Received a new web socket connection");
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        if (headerAccessor.getSessionAttributes() != null) {
            String userEmail = (String) headerAccessor.getSessionAttributes().get("userEmail");
            if (userEmail != null) {
                onlineUsers.put(userEmail, headerAccessor.getSessionId());
                String userId = (String) headerAccessor.getSessionAttributes().get("userId");

                this.notifyFriends(userId, userEmail, true);
            }
        }
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        if (headerAccessor.getSessionAttributes() != null) {
            String userEmail = (String) headerAccessor.getSessionAttributes().get("userEmail");
            if (userEmail != null) {
                onlineUsers.remove(userEmail);
                String userId = (String) headerAccessor.getSessionAttributes().get("userId");

                this.notifyFriends(userId, userEmail, false);
            }
        }
    }

    private void notifyFriends(String userId, String userEmail, boolean isOnline) {
        // Ý tưởng là lấy danh sách bạn bè ra -> Gửi trạng thái của mình cho từng người bạn

        List<String> friendEmails = friendService.getFriendEmails(userEmail);

        Map<String, Object> payload = new HashMap<>();
        payload.put("userId", userId); // Do bên frontend interface chỉ có id và displayname nên cần truyền id để FE so sánh mà hiện đúng trạng thái
        payload.put("isOnline", isOnline);

        for (String friendEmail : friendEmails) {
            simpMessagingTemplate.convertAndSendToUser(
                    friendEmail,
                    "/queue/friends/online-status",
                    payload
            );
        }
    }
}
