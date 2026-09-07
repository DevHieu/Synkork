package com.synkork.backend.modules.friend;

import com.synkork.backend.modules.friend.enums.FriendRequestStatus;
import com.synkork.backend.modules.notification.NotificationService;
import com.synkork.backend.modules.notification.enums.NotificationRefTypeEnum;
import com.synkork.backend.modules.notification.enums.NotificationTypeEnum;
import com.synkork.backend.modules.room.RoomService;
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

    @Autowired
    private RoomService roomService;

    @Autowired NotificationService notificationService;

    // Chấp nhận lời mời
    @Transactional
    public List<String> acceptRequest(UUID requestId) {
        FriendRequestEntity req = requestRepo.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy lời mời"));

        UUID conversationId = roomService.createDMRoom(req.getSender(), req.getReceiver());

        friendRepo.save(new FriendEntity(null, req.getSender(), req.getReceiver(), conversationId, null));
        friendRepo.save(new FriendEntity(null, req.getReceiver(), req.getSender(), conversationId, null));

        // notificationService.sendFriendNotification(req.getReceiver(), req.getSender(), requestId, NotificationRefTypeEnum.FRIEND_ACCEPT);

        requestRepo.delete(req);

        notificationService.sendNotification(req.getReceiver(), req.getSender(), requestId, null, null, NotificationTypeEnum.FRIEND, NotificationRefTypeEnum.FRIEND_ACCEPT);
        
       return List.of(req.getSender().getEmail(), req.getReceiver().getEmail());
    }

    // Từ chối lời mời — xóa luôn để người gửi có thể gửi lại sau
    public String rejectRequest(UUID requestId) {
        FriendRequestEntity req = requestRepo.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy lời mời"));
        String senderEmail = req.getSender().getEmail(); // lấy email trước khi xóa
        requestRepo.delete(req);

        notificationService.sendNotification(req.getReceiver(), req.getSender(), requestId, null, null, NotificationTypeEnum.FRIEND, NotificationRefTypeEnum.FRIEND_REJECT);
        return senderEmail; // ← thêm dòng này
    }

    // Hủy lời mời đã gửi
    public String cancelRequest(UUID requestId) {
        FriendRequestEntity req = requestRepo.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy lời mời"));

        UUID currentUserId = getCurrentUserId();
        if (!req.getSender().getId().equals(currentUserId)) {
            throw new RuntimeException("Bạn không có quyền hủy lời mời này");
        }

        requestRepo.delete(req);

        // Gửi email ra để gửi realtime cho thak nhận
        return req.getReceiver().getEmail();
    }

    // Lấy danh sách bạn bè
    public List<FriendEntity> getFriends(UUID userId) {
        UserEntity user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));
        return friendRepo.findByUser(user);
    }

    // Xóa bạn (2 chiều)
    @Transactional
    public List<String> removeFriend(UUID userId, UUID friendId) {
        UserEntity user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));
        UserEntity friend = userRepo.findById(friendId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bạn bè"));

        friendRepo.deleteByUserAndFriend(user, friend);
        friendRepo.deleteByUserAndFriend(friend, user);

        return List.of(user.getEmail(), friend.getEmail());
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
    public String sendFriendRequestByUsername(String username) {
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

        // Kiểm tra A đã gửi B chưa
        requestRepo.findBySenderAndReceiver(sender, receiver).ifPresent(r -> {
            throw new RuntimeException("Bạn đã gửi lời mời cho người này rồi");
        });

        // Kiểm tra B đã gửi A chưa
        requestRepo.findBySenderAndReceiver(receiver, sender).ifPresent(r -> {
            throw new RuntimeException(
                    "Người này đã gửi lời mời kết bạn cho bạn. Hãy vào mục lời mời để chấp nhận."
            );
        });

        FriendRequestEntity req = new FriendRequestEntity();
        req.setSender(sender);
        req.setReceiver(receiver);
        req.setStatus(FriendRequestStatus.PENDING);
        requestRepo.save(req);

        notificationService.sendNotification(req.getSender(), req.getReceiver(), null, null, null, NotificationTypeEnum.FRIEND, NotificationRefTypeEnum.FRIEND_REQUEST);

        // Trả về email de lam socket
        return receiver.getEmail();
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

    public List<String> getFriendEmails(String userId) {
        UUID userUUID = UUID.fromString(userId);
        return friendRepo.findFriendEmailByUserId(userUUID);
    }
}