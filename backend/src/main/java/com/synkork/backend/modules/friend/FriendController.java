package com.synkork.backend.modules.friend;

import com.synkork.backend.modules.friend.dto.FriendDto;
import com.synkork.backend.modules.friend.dto.FriendRequestDto;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/friends")
public class FriendController {

    @Autowired
    private FriendService friendService;

    // ==================== GỬI LỜI MỜI ====================
    @PostMapping("/request")
    public ResponseEntity<String> sendFriendRequest(@RequestParam String username) {
        try {
            friendService.sendFriendRequestByUsername(username);
            return ResponseEntity.ok("Đã gửi lời mời kết bạn");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ==================== HỦY LỜI MỜI ĐÃ GỬI ====================
    @DeleteMapping("/request/{requestId}")
    public ResponseEntity<String> cancelRequest(@PathVariable UUID requestId) {
        try {
            friendService.cancelRequest(requestId);
            return ResponseEntity.ok("Đã hủy lời mời kết bạn");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ==================== CHẤP NHẬN LỜI MỜI ====================
    @Transactional
    @PostMapping("/accept/{requestId}")
    public ResponseEntity<String> accept(@PathVariable UUID requestId) {
        try {
            friendService.acceptRequest(requestId);
            return ResponseEntity.ok("Đã chấp nhận lời mời kết bạn");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ==================== TỪ CHỐI LỜI MỜI ====================
    @PostMapping("/reject/{requestId}")
    public ResponseEntity<String> reject(@PathVariable UUID requestId) {
        try {
            friendService.rejectRequest(requestId);
            return ResponseEntity.ok("Đã từ chối lời mời");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ==================== LẤY DANH SÁCH BẠN BÈ ====================
    @GetMapping("/{userId}")
    public ResponseEntity<List<FriendDto>> getFriends(@PathVariable UUID userId) {
        List<FriendEntity> friends = friendService.getFriends(userId);

        List<FriendDto> dtos = friends.stream().map(f -> new FriendDto(
                f.getFriend().getId(),
                f.getFriend().getDisplayName() != null
                        ? f.getFriend().getDisplayName()
                        : f.getFriend().getUsername(),
                f.getFriend().getAvatarUrl(),
                "offline"
        )).toList();

        return ResponseEntity.ok(dtos);
    }

    // ==================== XÓA BẠN ====================
    @DeleteMapping
    public ResponseEntity<String> removeFriend(
            @RequestParam UUID userId,
            @RequestParam UUID friendId) {
        try {
            friendService.removeFriend(userId, friendId);
            return ResponseEntity.ok("Đã xóa bạn bè thành công");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ==================== LẤY LỜI MỜI ĐÃ NHẬN (PENDING) ====================
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

    // ==================== LẤY LỜI MỜI ĐÃ GỬI (PENDING) ====================
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