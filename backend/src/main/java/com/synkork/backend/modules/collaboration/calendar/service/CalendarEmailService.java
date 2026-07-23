package com.synkork.backend.modules.collaboration.calendar.service;

import com.synkork.backend.common.utils.EmailService;
import com.synkork.backend.modules.collaboration.calendar.entity.CalendarEventEntity;
import com.synkork.backend.modules.roomMember.RoomMemberEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CalendarEmailService {

    private final EmailService emailService;

    @Value("${frontend.client.url}")
    private String frontendUrl;

    @Async
    public void sendEventNotificationEmail(CalendarEventEntity event, List<RoomMemberEntity> recipients, boolean isReminder) {
        if (recipients == null || recipients.isEmpty()) return;

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy").withZone(ZoneId.of("Asia/Ho_Chi_Minh"));
        String timeStr = event.getStartTime() + " " + event.getEventDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

        String attendeesList = event.getAttendees().stream()
                .filter(a -> a.getUser() != null)
                .map(a -> a.getUser().getDisplayName() + " (" + a.getUser().getEmail() + ")")
                .collect(Collectors.joining("<br/>"));

        String callRoom = event.getCallRoomSpace() != null ? frontendUrl + "/spaces/" + event.getCallRoomSpace().getId() : "Không có";

        String filesList = event.getAttachments() != null && !event.getAttachments().isEmpty() ? event.getAttachments().stream()
                .map(a -> "<a href='" + a.getFileUrl() + "'>" + a.getFileName() + "</a>")
                .collect(Collectors.joining("<br/>")) : "Không có";

        String subject = isReminder ? "[Synkork] Nhắc nhở sự kiện sắp diễn ra: " + event.getTitle() : "[Synkork] Bạn được mời tham gia sự kiện: " + event.getTitle();

        String body = """
                <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 24px; border: 1px solid #e5e7eb; border-radius: 12px;">
                    <h2 style="color: #023c3d;">%s</h2>
                    <p><strong>Thời gian:</strong> %s</p>
                    <p><strong>Link phòng họp:</strong> <a href="%s">%s</a></p>
                    <p><strong>Người tham gia:</strong><br/> %s</p>
                    <p><strong>Tài liệu đính kèm:</strong><br/> %s</p>
                    <hr style="border: none; border-top: 1px solid #e5e7eb; margin: 24px 0 16px;"/>
                    <p style="margin: 0; font-size: 12px; color: #9ca3af; text-align: center;">Đây là email tự động từ Synkork — vui lòng không reply.</p>
                </div>
                """.formatted(event.getTitle(), timeStr, callRoom, callRoom, attendeesList, filesList);

        for (RoomMemberEntity member : recipients) {
            if (member.getUser() != null && member.getUser().getEmail() != null) {
                // ponytail: call the generic method from the shared EmailService directly
                emailService.send(member.getUser().getEmail(), subject, body);
            }
        }
    }
}
