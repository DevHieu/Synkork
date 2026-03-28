package com.synkork.backend.modules.friend;

import com.synkork.backend.modules.friend.dto.FriendDto;
import com.synkork.backend.modules.friend.dto.FriendRequestDto;
import jakarta.transaction.Transactional;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/friends")
@CrossOrigin(origins = "http://localhost:5173")
public class FriendController {

    private final FriendService friendService;

    public FriendController(FriendService friendService) {
        this.friendService = friendService;
    }

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

    // ==================== CHẤP NHẬN LỜI MỜI ====================
    @Transactional
    @PostMapping("/accept/{requestId}")
    public ResponseEntity<String> accept(@PathVariable Long requestId) {
        try {
            friendService.acceptRequest(requestId);
            return ResponseEntity.ok("Đã chấp nhận lời mời kết bạn");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ==================== TỪ CHỐI LỜI MỜI ====================
    @PostMapping("/reject/{requestId}")
    public ResponseEntity<String> reject(@PathVariable Long requestId) {
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

        // Chuyển đổi sang DTO để tránh vòng lặp JSON
        List<FriendDto> dtos = friends.stream().map(f -> new FriendDto(
                f.getFriend().getId(),
                f.getFriend().getUsername(),
                f.getFriend().getDisplayName(),
                f.getFriend().getAvatarUrl()
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

    // ==================== LẤY LỜI MỜI ĐANG CHỜ (cho phần thông báo) ====================
    @GetMapping("/requests/pending")
    public ResponseEntity<List<FriendRequestDto>> getPendingRequests() {
        try {
            UUID currentUserId = friendService.getCurrentUserId(); // bạn đã có method này trong service
            List<FriendRequestEntity> requests = friendService.getPending(currentUserId);

            List<FriendRequestDto> dtos = requests.stream().map(req ->
                    new FriendRequestDto(
                            req.getId(),
                            req.getSender().getUsername(),
                            req.getReceiver().getUsername(),
                            req.getStatus().name()
                    )
            ).toList();

            return ResponseEntity.ok(dtos);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}