package com.synkork.backend.modules.notification;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.synkork.backend.modules.notification.enums.NotificationRefTypeEnum;
import com.synkork.backend.modules.notification.enums.NotificationTypeEnum;
import com.synkork.backend.modules.user.UserEntity;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationService {
    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    // CARD_ASSIGNEE
    public void sendCardAssignedNotification(UserEntity actor, UserEntity target, UUID cardId, UUID roomId,
            UUID spaceId) {
        NotificationEntity noti = NotificationEntity.builder()
                .user(target)
                .actor(actor)
                .type(NotificationTypeEnum.TASK)
                .refId(cardId)
                .refType(NotificationRefTypeEnum.CARD_ASSIGNED)
                .isRead(false)
                .build();

        notificationRepository.save(noti);

        NotificationDTO dto = toDTOAssignee(noti, roomId, spaceId);

        System.out.println("SEND NOTI TO: " + target.getId());

        messagingTemplate.convertAndSendToUser(target.getEmail(), "/queue/notifications", dto);
    }

    // ADD FRIEND
    public void sendFriendNotification(UserEntity actor, UserEntity target, UUID friendRequestId, NotificationRefTypeEnum refType) {
        NotificationEntity noti = NotificationEntity.builder()
                .user(target)
                .actor(actor)
                .type(NotificationTypeEnum.FRIEND)
                .refId(friendRequestId)
                .refType(refType)
                .isRead(false)
                .build();

        notificationRepository.save(noti);

        NotificationDTO dto = toDTO(noti, null);

        messagingTemplate.convertAndSendToUser(target.getEmail(), "/queue/notifications", dto);
    }

    public void sendNotification(UserEntity actor, UserEntity target, UUID id, UUID spaceId, NotificationTypeEnum type, NotificationRefTypeEnum refType) {
        NotificationEntity noti = NotificationEntity.builder()
                .user(target)
                .actor(actor)
                .type(type)
                .refId(id)
                .refType(refType)
                .isRead(false)
                .build();

        notificationRepository.save(noti);

        NotificationDTO dto = toDTO(noti, spaceId);

        messagingTemplate.convertAndSendToUser(target.getEmail(), "/queue/notifications", dto);
    }

    // DUE_SOON
    public void sendCardDueSoonNotification(UserEntity actor, UserEntity target, UUID cardId, UUID roomId,
            UUID spaceId) {
        NotificationEntity noti = NotificationEntity.builder()
                .user(target)
                .actor(actor)
                .type(NotificationTypeEnum.TASK)
                .refId(cardId)
                .refType(NotificationRefTypeEnum.CARD_DUE_SOON)
                .isRead(false)
                .build();

        notificationRepository.save(noti);

        NotificationDTO dto = toDTOAssignee(noti, spaceId, roomId);

        messagingTemplate.convertAndSendToUser(target.getEmail(), "/queue/notifications", dto);
    }

    // OVER_DUE
    public void sendCardOverDueNotification(UserEntity actor, UserEntity target, UUID cardId, UUID roomId,
            UUID spaceId) {
        NotificationEntity noti = NotificationEntity.builder()
                .user(target)
                .actor(actor)
                .type(NotificationTypeEnum.TASK)
                .refId(cardId)
                .refType(NotificationRefTypeEnum.CARD_OVER_DUE)
                .isRead(false)
                .build();

        notificationRepository.save(noti);

        NotificationDTO dto = toDTOAssignee(noti, spaceId, roomId);

        messagingTemplate.convertAndSendToUser(target.getEmail(), "/queue/notifications", dto);
    }

    // CALENDAR
    public void sendCalendarEventNotification(UserEntity actor, UserEntity target, UUID calenderId, UUID spaceId) {
        NotificationEntity noti = NotificationEntity.builder()
                .user(target)
                .actor(actor)
                .type(NotificationTypeEnum.CALENDAR)
                .refId(calenderId)
                .refType(NotificationRefTypeEnum.EVENT_REMINDER)
                .isRead(false)
                .build();

        notificationRepository.save(noti);

        NotificationDTO dto = toDTO(noti, spaceId);

        messagingTemplate.convertAndSendToUser(target.getEmail(), "/queue/notifications", dto);
    }

    public void markAsRead(UUID notificationId, UUID userId) {
        NotificationEntity n = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));

        if (!n.getUser().getId().equals(userId)) {
            throw new RuntimeException("Forbidden");
        }

        n.setRead(true);
        notificationRepository.save(n);
    }

    public List<NotificationDTO> getNotifications(UUID userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream().map(n -> toDTO(n, null)).toList();
    }

    private NotificationDTO toDTO(NotificationEntity n, UUID spaceId) {
        return NotificationDTO.builder()
                .id(n.getId())
                .type(n.getType())
                .refType(n.getRefType())
                .refId(n.getRefId())
                .actorName(n.getActor() != null ? n.getActor().getDisplayName() : null)
                .actorAvatar(n.getActor() != null ? n.getActor().getAvatarUrl() : null)
                .isRead(n.isRead())
                .createdAt(n.getCreatedAt())
                .spaceId(spaceId) // để frontend navigate
                .build();
    }

    private NotificationDTO toDTOAssignee(NotificationEntity n, UUID roomId, UUID spaceId) {
        return NotificationDTO.builder()
                .id(n.getId())
                .type(n.getType())
                .refType(n.getRefType())
                .refId(n.getRefId())
                .actorName(n.getActor() != null ? n.getActor().getDisplayName() : null)
                .actorAvatar(n.getActor() != null ? n.getActor().getAvatarUrl() : null)
                .isRead(n.isRead())
                .createdAt(n.getCreatedAt())
                .spaceId(spaceId) // để frontend navigate
                .roomId(roomId)
                .build();
    }

    @Transactional
    public void deleteNotication(UUID notiId){
        NotificationEntity noti = notificationRepository.findById(notiId)
                .orElseThrow(() -> new RuntimeException("Thông báo không tồn tại"));
        
        notificationRepository.delete(noti);
    }
}
