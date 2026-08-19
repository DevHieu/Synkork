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
import com.synkork.backend.modules.space.SpaceEntity;
import com.synkork.backend.modules.user.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CalendarEmailService {

    private final EmailService emailService;
    private final CalendarEventRepository calendarEventRepository;
    private final NotificationService notificationService;
    private final EventUtils eventUtils;

    @Async
    @Transactional(readOnly = true)
    public void sendEventNotificationEmail(CalendarEventEntity event, List<RoomMemberEntity> recipients, boolean isReminder) {
        if (event == null || event.getId() == null) return;

        // Re-fetch entity từ DB bằng EntityGraph để tải EAGER hoàn toàn (tránh LazyInitializationException trong thread ngầm)
        CalendarEventEntity targetEvent = calendarEventRepository.findById(event.getId()).orElse(event);
        if (targetEvent.getSpace() == null || targetEvent.getSpace().getRoom() == null) return;

        List<RoomMemberEntity> attendeeList = (recipients != null && !recipients.isEmpty()) ? recipients : targetEvent.getAttendees();
        if (attendeeList == null || attendeeList.isEmpty()) return;

        for (RoomMemberEntity attendee : attendeeList) {
            if (attendee.getUser() == null || attendee.getUser().getEmail() == null) continue;

            EventEmailInformation info = eventUtils.buildEventEmailInformation(targetEvent, attendee, isReminder);
            this.sendEmailDirect(info);

            SpaceEntity space = targetEvent.getSpace();
            this.sendNotification(isReminder, attendee.getUser(), targetEvent.getCreatedBy(), targetEvent.getId(), space.getRoom().getId(), space.getId());
        }
    }

    private void sendEmailDirect(EventEmailInformation info) {
        String subject = info.isReminder()
                ? "[Synkork] Nhắc nhở sự kiện sắp diễn ra: " + info.title()
                : "[Synkork] Bạn được mời tham gia sự kiện: " + info.title();

        String taskBlock = info.taskInfo() != null
                ? String.format("<p style=\"margin: 6px 0;\"><strong>Thẻ công việc liên kết:</strong> %s</p>", info.taskInfo())
                : "";

        String noteBlock = info.noteInfo() != null
                ? String.format("<p style=\"margin: 6px 0;\"><strong>Ghi chú liên kết:</strong> %s</p>", info.noteInfo())
                : "";

        String callRoomBlock = info.callRoomUrl() != null
                ? String.format("<p style=\"margin: 6px 0;\"><strong>Link phòng họp Voice:</strong> <a href=\"%s\" style=\"color: #023c3d; font-weight: bold;\">%s</a></p>", info.callRoomUrl(), info.callRoomName())
                : "";

        String body = """
                <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 24px; border: 1px solid #e5e7eb; border-radius: 12px; background-color: #ffffff;">
                    <div style="border-bottom: 2px solid #023c3d; padding-bottom: 12px; margin-bottom: 20px;">
                        <span style="font-size: 12px; font-weight: bold; color: #023c3d; text-transform: uppercase; letter-spacing: 1px;">Phòng: %s &nbsp;|&nbsp; Kênh: %s</span>
                        <h2 style="color: #023c3d; margin: 8px 0 0 0; font-size: 22px;">%s</h2>
                    </div>

                    <div style="margin-bottom: 20px; line-height: 1.6; color: #374151;">
                        <p style="margin: 6px 0;"><strong>Người tạo:</strong> %s</p>
                        <p style="margin: 6px 0;"><strong>Thời gian:</strong> %s</p>
                        <p style="margin: 6px 0;"><strong>Chu kỳ lặp:</strong> %s</p>
                        <p style="margin: 6px 0;"><strong>Mô tả sự kiện:</strong><br/> <span style="color: #4b5563;">%s</span></p>
                        %s
                        %s
                        %s
                    </div>

                    <div style="background-color: #f9fafb; padding: 16px; border-radius: 8px; margin-bottom: 16px; border: 1px solid #f3f4f6;">
                        <p style="margin: 0 0 8px 0; font-weight: bold; color: #111827;">Thành viên tham gia (%d):</p>
                        <div style="font-size: 14px; color: #4b5563; line-height: 1.6;">%s</div>
                    </div>

                    <div style="background-color: #f9fafb; padding: 16px; border-radius: 8px; margin-bottom: 20px; border: 1px solid #f3f4f6;">
                        <p style="margin: 0 0 8px 0; font-weight: bold; color: #111827;">Tài liệu đính kèm:</p>
                        <div style="font-size: 14px; line-height: 1.6;">%s</div>
                    </div>

                    <hr style="border: none; border-top: 1px solid #e5e7eb; margin: 24px 0 16px;"/>
                    <p style="margin: 0; font-size: 12px; color: #9ca3af; text-align: center;">Đây là email tự động từ Synkork — vui lòng không reply.</p>
                </div>
                """.formatted(
                        info.roomName(), info.spaceName(), info.title(),
                        info.creatorName(), info.timeStr(), info.recurrenceStr(), info.description(),
                        taskBlock, noteBlock, callRoomBlock,
                        info.totalAttendees(), info.attendeeList(),
                        info.fileList()
                );

        emailService.send(info.recipientEmail(), subject, body);
    }

    private void sendNotification(boolean isReminder, UserEntity target, UserEntity actor, UUID eventId, UUID roomId, UUID spaceId) {
        NotificationRefTypeEnum refType = isReminder ? NotificationRefTypeEnum.EVENT_REMINDER : NotificationRefTypeEnum.EVENT_ASSIGNED;
        notificationService.sendNotification(actor, target, eventId, roomId, spaceId, NotificationTypeEnum.CALENDAR, refType);
    }
}
