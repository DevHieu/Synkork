package com.synkork.backend.modules.notification;

import java.time.LocalDateTime;
import java.util.UUID;

import com.synkork.backend.modules.notification.enums.NotificationRefTypeEnum;
import com.synkork.backend.modules.notification.enums.NotificationTypeEnum;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NotificationDTO {
    private UUID id;
    private NotificationTypeEnum type;
    private NotificationRefTypeEnum refType;
    private UUID refId;
    private String actorName;
    private String actorAvatar;
    private boolean isRead;
    private LocalDateTime createdAt;
    private UUID spaceId;  // để frontend biết navigate về đâu
    private UUID roomId;
}
