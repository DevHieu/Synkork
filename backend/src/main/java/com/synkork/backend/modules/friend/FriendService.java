package com.synkork.backend.modules.friend;

import com.synkork.backend.modules.friend.enums.FriendRequestStatus;
import com.synkork.backend.modules.user.UserEntity;
import com.synkork.backend.modules.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FriendService {

    private final FriendRepository friendRepo;
    private final FriendRequestRepository requestRepo;
    private final UserRepository userRepo;

    // Gửi lời mời kết bạn
    public void sendRequest(UUID senderId, UUID receiverId, String message) {
        UserEntity sender = userRepo.findById(senderId).orElseThrow(() -> new RuntimeException("Không tìm thấy người gửi"));
        UserEntity receiver = userRepo.findById(receiverId).orElseThrow(() -> new RuntimeException("Không tìm thấy người nhận"));

        if (requestRepo.findBySenderAndReceiver(sender, receiver).isPresent()) {
            throw new RuntimeException("Đã gửi lời mời trước đó");
        }

        FriendRequestEntity req = new FriendRequestEntity();
        req.setSender(sender);
        req.setReceiver(receiver);
        req.setStatus(FriendRequestStatus.PENDING);
        req.setMessage(message);

        requestRepo.save(req);
    }

    // Chấp nhận lời mời
    public void acceptRequest(Long requestId) {
        FriendRequestEntity req = requestRepo.findById(requestId).orElseThrow();
        req.setStatus(FriendRequestStatus.ACCEPTED);

        // Tạo bạn bè 2 chiều
        friendRepo.save(new FriendEntity(null, req.getSender(), req.getReceiver(), null));
        friendRepo.save(new FriendEntity(null, req.getReceiver(), req.getSender(), null));

        requestRepo.save(req);
    }

    // Từ chối lời mời
    public void rejectRequest(Long requestId) {
        FriendRequestEntity req = requestRepo.findById(requestId).orElseThrow();
        req.setStatus(FriendRequestStatus.REJECTED);
        requestRepo.save(req);
    }

    // Lấy danh sách bạn bè
    public List<FriendEntity> getFriends(UUID userId) {
        UserEntity user = userRepo.findById(userId).orElseThrow();
        return friendRepo.findByUser(user);
    }

    // Xóa bạn
    public void removeFriend(UUID userId, UUID friendId) {
        UserEntity user = userRepo.findById(userId).orElseThrow();
        UserEntity friend = userRepo.findById(friendId).orElseThrow();

        friendRepo.deleteByUserAndFriend(user, friend);
        friendRepo.deleteByUserAndFriend(friend, user);
    }

    // Lấy lời mời đang chờ
    public List<FriendRequestEntity> getPending(UUID userId) {
        UserEntity user = userRepo.findById(userId).orElseThrow();
        return requestRepo.findByReceiverAndStatus(user, FriendRequestStatus.PENDING);
    }

    public void sendFriendRequestByUsername(String username) {
        UUID senderId = getCurrentUserId();

        UserEntity sender = userRepo.findById(senderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người gửi"));

        // Chuyển username về lowercase để tìm không phân biệt hoa thường
        String searchUsername = username.trim().toLowerCase();

        UserEntity receiver = userRepo.findByUsername(searchUsername)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng với username: " + username));

        if (sender.getId().equals(receiver.getId())) {
            throw new RuntimeException("Không thể tự kết bạn với chính mình");
        }

        if (requestRepo.findBySenderAndReceiver(sender, receiver).isPresent()) {
            throw new RuntimeException("Bạn đã gửi lời mời cho người này rồi");
        }

        FriendRequestEntity req = new FriendRequestEntity();
        req.setSender(sender);
        req.setReceiver(receiver);
        req.setStatus(FriendRequestStatus.PENDING);
        requestRepo.save(req);
    }

    public UUID getCurrentUserId() {
        String email = org.springframework.security.core.context.SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        return userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"))
                .getId();
    }
}