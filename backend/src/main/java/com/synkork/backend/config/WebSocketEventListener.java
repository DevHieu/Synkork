package com.synkork.backend.config;

import com.synkork.backend.modules.friend.FriendService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

// Lớp này lắng nghe các sự kiện kết nối và ngắt kết nối WebSocket
@Component
public class WebSocketEventListener {
    private static final Logger logger = LoggerFactory.getLogger(WebSocketEventListener.class);

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    @Autowired
    private FriendService friendService;

    // Đếm số lượng user unique online trong ngày (tích lũy, reset mỗi ngày bởi StatisticsService)
    public static long onlineUserCounter = 0;

    // Lưu lại các user online: userId -> Set<sessionId>
    // Dùng ConcurrentHashMap để thread-safe với nhiều connect/disconnect đồng thời
    // Set<sessionId> để hỗ trợ user mở nhiều tab cùng lúc
    public static Map<String, Set<String>> onlineUsers = new ConcurrentHashMap<>();

    // Lưu các userId đã được đếm hôm nay — tránh đếm lại khi user F5 (disconnect rồi reconnect)
    public static Set<String> countedUsers = ConcurrentHashMap.newKeySet();

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectEvent event) {
        logger.info("Received a new web socket connection");
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        if (headerAccessor.getSessionAttributes() != null) {
            String userId = (String) headerAccessor.getSessionAttributes().get("userId");
            String sessionId = headerAccessor.getSessionId();
            if (userId != null) {
                // Thêm sessionId vào Set của userId
                onlineUsers.computeIfAbsent(userId, k -> ConcurrentHashMap.newKeySet()).add(sessionId);

                this.notifyFriends(userId, true);

                // Chỉ tăng counter nếu user chưa được đếm hôm nay (kể cả F5 disconnect rồi reconnect)
                if (countedUsers.add(userId)) {
                    onlineUserCounter++;
                }
            }
        }
        System.out.println("Số người online trong ngày: " + onlineUserCounter);
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        if (headerAccessor.getSessionAttributes() != null) {
            String userId = (String) headerAccessor.getSessionAttributes().get("userId");
            String sessionId = headerAccessor.getSessionId();
            if (userId != null) {
                Set<String> sessions = onlineUsers.get(userId);
                if (sessions != null) {
                    sessions.remove(sessionId);

                    // Chỉ coi user là offline khi không còn session nào
                    if (sessions.isEmpty()) {
                        onlineUsers.remove(userId);
                        this.notifyFriends(userId, false);
                    }
                }
            }
        }
    }

    private void notifyFriends(String userId, boolean isOnline) {
        // Ý tưởng là lấy danh sách bạn bè ra -> Gửi trạng thái của mình cho từng người bạn

        List<String> friendEmails = friendService.getFriendEmails(userId);

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

    public boolean isOnline(UUID userId) {
        return onlineUsers.containsKey(userId.toString());
    }
}
