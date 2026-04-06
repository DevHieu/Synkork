package com.synkork.backend.modules.friend;

import com.synkork.backend.modules.friend.enums.FriendRequestStatus;
import com.synkork.backend.modules.user.UserEntity;
import com.synkork.backend.modules.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class FriendService {

    @Autowired
    private FriendRepository friendRepo;

    @Autowired
    private FriendRequestRepository requestRepo;

    @Autowired
    private UserRepository userRepo;

    // Gửi lời mời kết bạn
    public void sendRequest(UUID senderId, UUID receiverId, String message) {
        UserEntity sender = userRepo.findById(senderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người gửi"));
        UserEntity receiver = userRepo.findById(receiverId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người nhận"));

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
    public void acceptRequest(UUID requestId) {
        FriendRequestEntity req = requestRepo.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy lời mời"));

        friendRepo.save(new FriendEntity(null, req.getSender(), req.getReceiver(), null));
        friendRepo.save(new FriendEntity(null, req.getReceiver(), req.getSender(), null));

        requestRepo.delete(req);  // ← phải DELETE, không phải setStatus
    }

    // Từ chối lời mời — xóa luôn để người gửi có thể gửi lại sau
    public void rejectRequest(UUID requestId) {
        FriendRequestEntity req = requestRepo.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy lời mời"));
        requestRepo.delete(req);
    }

    // Hủy lời mời đã gửi
    public void cancelRequest(UUID requestId) {
        FriendRequestEntity req = requestRepo.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy lời mời"));

        UUID currentUserId = getCurrentUserId();
        if (!req.getSender().getId().equals(currentUserId)) {
            throw new RuntimeException("Bạn không có quyền hủy lời mời này");
        }

        requestRepo.delete(req);
    }

    // Lấy danh sách bạn bè
    public List<FriendEntity> getFriends(UUID userId) {
        UserEntity user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));
        return friendRepo.findByUser(user);
    }

    // Xóa bạn (2 chiều)
    @Transactional
    public void removeFriend(UUID userId, UUID friendId) {
        UserEntity user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));
        UserEntity friend = userRepo.findById(friendId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bạn bè"));

        friendRepo.deleteByUserAndFriend(user, friend);
        friendRepo.deleteByUserAndFriend(friend, user);
    }

    // Lấy lời mời đã nhận
    public List<FriendRequestEntity> getPending(UUID userId) {
        UserEntity user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));
        return requestRepo.findByReceiverAndStatus(user, FriendRequestStatus.PENDING);
    }

    // Lấy lời mời đã gửi
    public List<FriendRequestEntity> getSentRequests(UUID userId) {
        UserEntity user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));
        return requestRepo.findBySenderAndStatus(user, FriendRequestStatus.PENDING);
    }

    // Gửi lời mời qua username
    public void sendFriendRequestByUsername(String username) {
        UUID senderId = getCurrentUserId();

        UserEntity sender = userRepo.findById(senderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người gửi"));

        UserEntity receiver = userRepo.findByUsername(username.trim().toLowerCase())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng với username: " + username));

        if (sender.getId().equals(receiver.getId())) {
            throw new RuntimeException("Không thể tự kết bạn với chính mình");
        }

        // Kiểm tra đã là bạn chưa
        if (friendRepo.findByUserAndFriend(sender, receiver).isPresent()) {
            throw new RuntimeException("Hai bạn đã là bạn bè rồi");
        }

        // Chỉ chặn nếu đang có request PENDING — không chặn nếu đã bị reject/cancel (đã xóa)
        requestRepo.findBySenderAndReceiver(sender, receiver).ifPresent(r -> {
            throw new RuntimeException("Bạn đã gửi lời mời cho người này rồi");
        });

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