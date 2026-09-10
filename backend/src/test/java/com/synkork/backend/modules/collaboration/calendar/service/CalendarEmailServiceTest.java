package com.synkork.backend.modules.collaboration.calendar.service;

import com.synkork.backend.common.utils.EmailService;
import com.synkork.backend.modules.collaboration.calendar.dto.EventEmailInformation;
import com.synkork.backend.modules.collaboration.calendar.entity.CalendarEventEntity;
import com.synkork.backend.modules.collaboration.calendar.repository.CalendarEventRepository;
import com.synkork.backend.modules.collaboration.calendar.utils.EventUtils;
import com.synkork.backend.modules.notification.NotificationService;
import com.synkork.backend.modules.notification.enums.NotificationRefTypeEnum;
import com.synkork.backend.modules.notification.enums.NotificationTypeEnum;
import com.synkork.backend.modules.roomMember.RoomMemberEntity;
import com.synkork.backend.modules.user.UserEntity;
import com.synkork.backend.modules.space.SpaceEntity;
import com.synkork.backend.modules.room.RoomEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CalendarEmailServiceTest {

    @Mock
    private EmailService emailService;

    @Mock
    private CalendarEventRepository calendarEventRepository;

    @Mock
    private NotificationService notificationService;

    @Mock
    private EventUtils eventUtils;

    @InjectMocks
    private CalendarEmailService calendarEmailService;

    @Test
    void testSendEventNotificationEmail_NoRecipients() {
        UUID eventId = UUID.randomUUID();
        UUID roomId = UUID.randomUUID();
        RoomEntity room = new RoomEntity();
        room.setId(roomId);

        SpaceEntity space = new SpaceEntity();
        space.setRoom(room);

        CalendarEventEntity event = new CalendarEventEntity();
        event.setId(eventId);
        event.setSpace(space);

        when(calendarEventRepository.findById(eventId)).thenReturn(Optional.of(event));

        // Act
        calendarEmailService.sendEventNotificationEmail(event, Collections.emptyList(), false);

        // Assert
        verify(emailService, never()).send(anyString(), anyString(), anyString());
    }

    @Test
    void testSendEventNotificationEmail_Success() {
        UUID eventId = UUID.randomUUID();
        UUID roomId = UUID.randomUUID();
        UUID spaceId = UUID.randomUUID();
        RoomEntity room = new RoomEntity();
        room.setId(roomId);
        room.setName("Phòng Họp Test");

        SpaceEntity space = new SpaceEntity();
        space.setId(spaceId);
        space.setRoom(room);
        space.setName("Kênh Lịch Test");

        UserEntity user = new UserEntity();
        user.setId(UUID.randomUUID());
        user.setDisplayName("Test User");
        user.setEmail("test@example.com");

        UserEntity creator = new UserEntity();
        creator.setId(UUID.randomUUID());
        creator.setDisplayName("Creator");

        RoomMemberEntity recipient = new RoomMemberEntity();
        recipient.setUser(user);

        CalendarEventEntity event = new CalendarEventEntity();
        event.setId(eventId);
        event.setSpace(space);
        event.setCreatedBy(creator);
        event.setTitle("Important Meeting");

        EventEmailInformation emailInfo = EventEmailInformation.builder()
                .title("Important Meeting")
                .description("Mô tả")
                .roomName("Phòng Họp Test")
                .spaceName("Kênh Lịch Test")
                .creatorName("Creator")
                .timeStr("14:00 - 15:00")
                .recurrenceStr("Không lặp lại")
                .attendeeList("Test User")
                .fileList("")
                .totalAttendees(1)
                .recipientEmail("test@example.com")
                .isReminder(false)
                .build();

        when(calendarEventRepository.findById(eventId)).thenReturn(Optional.of(event));
        when(eventUtils.buildEventEmailInformation(event, recipient, false)).thenReturn(emailInfo);

        // Act
        calendarEmailService.sendEventNotificationEmail(event, List.of(recipient), false);

        // Assert
        verify(emailService, times(1)).send(
                eq("test@example.com"),
                eq("[Synkork] Bạn được mời tham gia sự kiện: Important Meeting"),
                anyString()
        );
        verify(notificationService, times(1)).sendNotification(
                eq(creator),
                eq(user),
                eq(eventId),
                eq(roomId),
                eq(spaceId),
                eq(NotificationTypeEnum.CALENDAR),
                eq(NotificationRefTypeEnum.EVENT_ASSIGNED)
        );
    }
}
