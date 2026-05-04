package com.synkork.backend.modules.friend;

import com.synkork.backend.config.WebSocketEventListener;
import com.synkork.backend.modules.friend.dto.FriendDto;
import com.synkork.backend.modules.friend.dto.FriendRequestDto;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/friends")
public class FriendController {

    @Autowired
    private FriendService friendService;

    @Autowired
    SimpMessagingTemplate messagingTemplate;

    @Autowired
    WebSocketEventListener webSocketEventListener;

    //  GỬI LỜI MỜI
    @PostMapping("/request")
    public ResponseEntity<String> sendFriendRequest(@RequestParam String username) {
        try {
            String email = friendService.sendFriendRequestByUsername(username);

            messagingTemplate.convertAndSendToUser(email, "queue/friend-request", "Đã gửi lời mời kết bạn");

            return ResponseEntity.ok("Đã gửi lời mời kết bạn");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


    //  HỦY LỜI MỜI ĐÃ GỬI
    @DeleteMapping("/request/{requestId}")
    public ResponseEntity<String> cancelRequest(@PathVariable UUID requestId) {
        try {
            String email = friendService.cancelRequest(requestId);
            // Fix: dùng đúng topic friend-cancel
            messagingTemplate.convertAndSendToUser(email, "queue/friend-cancel", "Lời mời đã bị hủy");
            return ResponseEntity.ok("Đã hủy lời mời kết bạn");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    //CHẤP NHẬN LỜI MỜI
    @Transactional
    @PostMapping("/accept/{requestId}")
    public ResponseEntity<String> accept(@PathVariable UUID requestId) {
        try {
            List<String> emails = friendService.acceptRequest(requestId);

            messagingTemplate.convertAndSendToUser(emails.get(0), "queue/friend-accept", "Đã chấp nhận lời mời kết bạn");
            messagingTemplate.convertAndSendToUser(emails.get(1), "queue/friend-accept", "Bạn đã chấp nhận lời mời");

            return ResponseEntity.ok("Đã chấp nhận lời mời kết bạn");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // TỪ CHỐI LỜI MỜI
    @PostMapping("/reject/{requestId}")
    public ResponseEntity<String> reject(@PathVariable UUID requestId) {
        try {
            String senderEmail = friendService.rejectRequest(requestId); // đổi void → String
            messagingTemplate.convertAndSendToUser(senderEmail, "queue/friend-reject", "Lời mời bị từ chối");
            return ResponseEntity.ok("Đã từ chối lời mời");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    //  LẤY DANH SÁCH BẠN BÈ
    @GetMapping("/{userId}")
    public ResponseEntity<List<FriendDto>> getFriends(@PathVariable UUID userId) {
        List<FriendEntity> friends = friendService.getFriends(userId);

        List<FriendDto> dtos = friends.stream().map(f -> new FriendDto(
                f.getFriend().getId(),
                f.getFriend().getDisplayName() != null
                        ? f.getFriend().getDisplayName()
                        : f.getFriend().getUsername(),
                f.getFriend().getAvatarUrl(),
                webSocketEventListener.isOnline(f.getFriend().getId())

        )).toList();

        return ResponseEntity.ok(dtos);
    }

    // XÓA BẠN
    @DeleteMapping
    public ResponseEntity<String> removeFriend(
            @RequestParam UUID userId,
            @RequestParam UUID friendId) {
        try {
            List<String> emails = friendService.removeFriend(userId, friendId);
            messagingTemplate.convertAndSendToUser(emails.get(0), "queue/friend-remove", "Đã xóa bạn bè thành công");
            messagingTemplate.convertAndSendToUser(emails.get(1), "queue/friend-remove", "Đã bị xóa khỏi danh sách bạn bè");
            return ResponseEntity.ok("Đã xóa bạn bè thành công");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    //  LẤY LỜI MỜI ĐÃ NHẬN (PENDING)
    @GetMapping("/requests/pending")
    public ResponseEntity<List<FriendRequestDto>> getPendingRequests() {
        try {
            UUID currentUserId = friendService.getCurrentUserId();
            List<FriendRequestEntity> requests = friendService.getPending(currentUserId);

            List<FriendRequestDto> dtos = requests.stream().map(req ->
                    new FriendRequestDto(
                            req.getId(),
                            req.getSender().getDisplayName() != null
                                    ? req.getSender().getDisplayName()
                                    : req.getSender().getUsername(),
                            req.getReceiver().getUsername(),
                            req.getStatus().name()
                    )
            ).toList();

            return ResponseEntity.ok(dtos);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    //  LẤY LỜI MỜI ĐÃ GỬI (PENDING)
    @GetMapping("/requests/sent")
    public ResponseEntity<List<FriendRequestDto>> getSentRequests() {
        try {
            UUID currentUserId = friendService.getCurrentUserId();
            List<FriendRequestEntity> requests = friendService.getSentRequests(currentUserId);

            List<FriendRequestDto> dtos = requests.stream().map(req ->
                    new FriendRequestDto(
                            req.getId(),
                            req.getSender().getUsername(),
                            req.getReceiver().getDisplayName() != null
                                    ? req.getReceiver().getDisplayName()
                                    : req.getReceiver().getUsername(),
                            req.getStatus().name()
                    )
            ).toList();

            return ResponseEntity.ok(dtos);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}