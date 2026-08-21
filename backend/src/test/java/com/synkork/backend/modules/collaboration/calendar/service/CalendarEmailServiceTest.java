package com.synkork.backend.modules.collaboration.calendar.service;

import com.synkork.backend.common.utils.EmailService;
import com.synkork.backend.modules.collaboration.calendar.entity.CalendarEventEntity;
import com.synkork.backend.modules.collaboration.calendar.repository.CalendarEventRepository;
import com.synkork.backend.modules.roomMember.RoomMemberEntity;
import com.synkork.backend.modules.roomMember.RoomMemberRepository;
import com.synkork.backend.modules.user.UserEntity;
import com.synkork.backend.modules.space.SpaceEntity;
import com.synkork.backend.modules.room.RoomEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CalendarEmailServiceTest {

    @Mock
    private EmailService emailService;

    @Mock
    private RoomMemberRepository roomMemberRepository;

    @Mock
    private CalendarEventRepository calendarEventRepository;

    @InjectMocks
    private CalendarEmailService calendarEmailService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(calendarEmailService, "frontendUrl", "http://localhost:3000");
    }

    @Test
    void testSendEventNotificationEmail_NoRecipients() {
        // Arrange
        UUID eventId = UUID.randomUUID();
        UUID roomId = UUID.randomUUID();
        RoomEntity room = new RoomEntity();
        room.setId(roomId);
        room.setName("Phòng Test");
        
        SpaceEntity space = new SpaceEntity();
        space.setRoom(room);
        space.setName("Kênh Test");
        
        CalendarEventEntity event = new CalendarEventEntity();
        event.setId(eventId);
        event.setSpace(space);

        when(calendarEventRepository.findById(eventId)).thenReturn(Optional.of(event));
        when(roomMemberRepository.findByRoom_Id(roomId)).thenReturn(Collections.emptyList());

        // Act
        calendarEmailService.sendEventNotificationEmail(event, Collections.emptyList(), false);

        // Assert
        verify(emailService, never()).send(anyString(), anyString(), anyString());
    }

    @Test
    void testSendEventNotificationEmail_Success() {
        // Arrange
        UUID eventId = UUID.randomUUID();
        UUID roomId = UUID.randomUUID();
        RoomEntity room = new RoomEntity();
        room.setId(roomId);
        room.setName("Phòng Họp Test");
        
        SpaceEntity space = new SpaceEntity();
        space.setRoom(room);
        space.setName("Kênh Lịch Test");

        UserEntity user = new UserEntity();
        user.setDisplayName("Test User");
        user.setEmail("test@example.com");

        RoomMemberEntity recipient = new RoomMemberEntity();
        recipient.setUser(user);

        CalendarEventEntity event = new CalendarEventEntity();
        event.setId(eventId);
        event.setSpace(space);
        event.setTitle("Important Meeting");
        event.setDescription("Let's discuss the project status.");
        event.setEventDate(LocalDate.of(2026, 7, 24));
        event.setStartTime(LocalTime.of(14, 0));
        
        SpaceEntity callRoom = new SpaceEntity();
        callRoom.setId(UUID.randomUUID());
        event.setCallRoomSpace(callRoom);

        when(calendarEventRepository.findById(eventId)).thenReturn(Optional.of(event));
        when(roomMemberRepository.findByRoom_Id(roomId)).thenReturn(List.of(recipient));

        // Act
        calendarEmailService.sendEventNotificationEmail(event, List.of(recipient), false);

        // Assert
        verify(emailService, times(1)).send(
                eq("test@example.com"),
                eq("[Synkork] Bạn được mời tham gia sự kiện: Important Meeting"),
                anyString()
        );
    }
}
