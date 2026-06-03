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

    public void sendNotification(UserEntity actor, UserEntity target, UUID id, UUID roomId, UUID spaceId, NotificationTypeEnum type, NotificationRefTypeEnum refType) {
        NotificationEntity noti = NotificationEntity.builder()
                .user(target)
                .actor(actor)
                .type(type)
                .refId(id)
                .refType(refType)
                .roomId(roomId)
                .spaceId(spaceId)
                .isRead(false)
                .build();

        notificationRepository.save(noti);

        NotificationDTO dto = toDTO(noti);  

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
                .stream().map(n -> toDTO(n)).toList();
    }

    private NotificationDTO toDTO(NotificationEntity n) {
        return NotificationDTO.builder()
                .id(n.getId())
                .type(n.getType())
                .refType(n.getRefType())
                .refId(n.getRefId())
                .actorName(n.getActor() != null ? n.getActor().getDisplayName() : null)
                .actorAvatar(n.getActor() != null ? n.getActor().getAvatarUrl() : null)
                .isRead(n.isRead())
                .createdAt(n.getCreatedAt())
                .roomId(n.getRoomId())
                .spaceId(n.getSpaceId())
                .build();
    }
    
    @Transactional
    public void deleteNotication(UUID notiId){
        NotificationEntity noti = notificationRepository.findById(notiId)
                .orElseThrow(() -> new RuntimeException("Thông báo không tồn tại"));
        
        notificationRepository.delete(noti);
    }
}
