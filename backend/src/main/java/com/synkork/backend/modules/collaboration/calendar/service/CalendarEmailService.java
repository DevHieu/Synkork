package com.synkork.backend.modules.collaboration.calendar.service;

import com.synkork.backend.common.utils.EmailService;
import com.synkork.backend.modules.collaboration.calendar.entity.CalendarEventEntity;
import com.synkork.backend.modules.collaboration.calendar.repository.CalendarEventRepository;
import com.synkork.backend.modules.roomMember.RoomMemberEntity;
import com.synkork.backend.modules.roomMember.RoomMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CalendarEmailService {

    private final EmailService emailService;
    private final RoomMemberRepository roomMemberRepository;
    private final CalendarEventRepository calendarEventRepository;

    @Value("${frontend.client.url}")
    private String frontendUrl;

    @Async
    @Transactional(readOnly = true)
    public void sendEventNotificationEmail(CalendarEventEntity event, List<RoomMemberEntity> recipients, boolean isReminder) {
        if (event == null || event.getId() == null) return;

        // Re-fetch entity từ DB bằng EntityGraph để tải EAGER hoàn toàn (tránh LazyInitializationException trong thread ngầm)
        CalendarEventEntity targetEvent = calendarEventRepository.findById(event.getId()).orElse(event);
        if (targetEvent == null || targetEvent.getSpace() == null || targetEvent.getSpace().getRoom() == null) return;

        // Lấy tất cả thành viên trong phòng (room)
        List<RoomMemberEntity> roomMembers = roomMemberRepository.findByRoom_Id(targetEvent.getSpace().getRoom().getId());
        if (roomMembers == null || roomMembers.isEmpty()) return;

        // Trích xuất toàn bộ thông tin chi tiết sự kiện
        String title = targetEvent.getTitle();
        String description = (targetEvent.getDescription() != null && !targetEvent.getDescription().isBlank())
                ? targetEvent.getDescription()
                : "Không có mô tả";

        String roomName = targetEvent.getSpace().getRoom().getName();
        String spaceName = targetEvent.getSpace().getName();
        String creatorName = targetEvent.getCreatedBy() != null ? targetEvent.getCreatedBy().getDisplayName() : "Hệ thống";

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

        String startTimeStr = targetEvent.getStartTime() != null ? targetEvent.getStartTime().format(timeFormatter) : "";
        String endTimeStr = targetEvent.getEndTime() != null ? targetEvent.getEndTime().format(timeFormatter) : "";
        String startDateStr = targetEvent.getEventDate() != null ? targetEvent.getEventDate().format(dateFormatter) : "";
        String endDateStr = targetEvent.getEndDate() != null ? targetEvent.getEndDate().format(dateFormatter) : startDateStr;

        String timeStr;
        if (startDateStr.equals(endDateStr)) {
            timeStr = String.format("%s - %s (%s)", startTimeStr, endTimeStr, startDateStr);
        } else {
            timeStr = String.format("%s (%s) đến %s (%s)", startTimeStr, startDateStr, endTimeStr, endDateStr);
        }

        String recurrenceStr = "Không lặp lại";
        if (targetEvent.getRecurrenceType() != null) {
            switch (targetEvent.getRecurrenceType()) {
                case "DAILY" -> recurrenceStr = "Hàng ngày";
                case "WEEKLY" -> recurrenceStr = "Hàng tuần";
                case "MONTHLY" -> recurrenceStr = "Hàng tháng";
                case "YEARLY" -> recurrenceStr = "Hàng năm";
                default -> {
                    if (!targetEvent.getRecurrenceType().equalsIgnoreCase("NONE")) {
                        recurrenceStr = targetEvent.getRecurrenceType();
                    }
                }
            }
        }

        String taskInfo = null;
        if (targetEvent.getTask() != null) {
            String spaceId = targetEvent.getTask().getColumn() != null && targetEvent.getTask().getColumn().getBoard() != null && targetEvent.getTask().getColumn().getBoard().getSpace() != null
                    ? targetEvent.getTask().getColumn().getBoard().getSpace().getId().toString()
                    : targetEvent.getSpace().getId().toString();
            String taskUrl = frontendUrl + "/spaces/" + spaceId + "?cardId=" + targetEvent.getTask().getId();
            taskInfo = "<a href='" + taskUrl + "' style='color: #023c3d; font-weight: bold; text-decoration: underline;'>" + targetEvent.getTask().getTitle() + "</a>";
        }

        String noteInfo = null;
        if (targetEvent.getNote() != null) {
            String noteSpaceId = targetEvent.getNote().getSpace() != null
                    ? targetEvent.getNote().getSpace().getId().toString()
                    : targetEvent.getSpace().getId().toString();
            String noteUrl = frontendUrl + "/spaces/" + noteSpaceId + "?noteId=" + targetEvent.getNote().getId();
            noteInfo = "<a href='" + noteUrl + "' style='color: #023c3d; font-weight: bold; text-decoration: underline;'>" + targetEvent.getNote().getTitle() + "</a>";
        }

        String callRoomUrl = targetEvent.getCallRoomSpace() != null
                ? frontendUrl + "/spaces/" + targetEvent.getCallRoomSpace().getId()
                : null;
        String callRoomName = targetEvent.getCallRoomSpace() != null
                ? targetEvent.getCallRoomSpace().getName()
                : null;

        String attendeesList = roomMembers.stream()
                .filter(a -> a.getUser() != null)
                .map(a -> a.getUser().getDisplayName() + " (" + a.getUser().getEmail() + ")")
                .collect(Collectors.joining("<br/>"));

        // Tạo thẻ div cho mỗi file đính kèm để chắc chắn xuống dòng rõ ràng trong email client
        String filesList = targetEvent.getAttachments() != null && !targetEvent.getAttachments().isEmpty() ? targetEvent.getAttachments().stream()
                .map(a -> "<div style='margin-bottom: 6px;'><a href='" + a.getFileUrl() + "' style='color: #023c3d; font-weight: bold; text-decoration: underline;'>" + a.getFileName() + "</a></div>")
                .collect(Collectors.joining()) : "Không có";

        List<String> recipientEmails = roomMembers.stream()
                .filter(member -> member.getUser() != null && member.getUser().getEmail() != null)
                .map(member -> member.getUser().getEmail())
                .toList();

        // Gửi mail với mẫu HTML đầy đủ thông tin
        sendEmailDirect(
                title,
                description,
                roomName,
                spaceName,
                creatorName,
                timeStr,
                recurrenceStr,
                taskInfo,
                noteInfo,
                callRoomUrl,
                callRoomName,
                attendeesList,
                filesList,
                roomMembers.size(),
                recipientEmails,
                isReminder
        );
    }

    private void sendEmailDirect(
            String title,
            String description,
            String roomName,
            String spaceName,
            String creatorName,
            String timeStr,
            String recurrenceStr,
            String taskInfo,
            String noteInfo,
            String callRoomUrl,
            String callRoomName,
            String attendeesList,
            String filesList,
            int totalAttendees,
            List<String> recipientEmails,
            boolean isReminder
    ) {
        if (recipientEmails.isEmpty()) return;

        String subject = isReminder
                ? "[Synkork] Nhắc nhở sự kiện sắp diễn ra: " + title
                : "[Synkork] Bạn được mời tham gia sự kiện: " + title;

        String taskBlock = taskInfo != null
                ? String.format("<p style=\"margin: 6px 0;\"><strong>Thẻ công việc liên kết:</strong> %s</p>", taskInfo)
                : "";

        String noteBlock = noteInfo != null
                ? String.format("<p style=\"margin: 6px 0;\"><strong>Ghi chú liên kết:</strong> %s</p>", noteInfo)
                : "";

        String callRoomBlock = callRoomUrl != null
                ? String.format("<p style=\"margin: 6px 0;\"><strong>Link phòng họp Voice:</strong> <a href=\"%s\" style=\"color: #023c3d; font-weight: bold;\">%s</a></p>", callRoomUrl, callRoomName)
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
                        roomName, spaceName, title,
                        creatorName, timeStr, recurrenceStr, description,
                        taskBlock, noteBlock, callRoomBlock,
                        totalAttendees, attendeesList,
                        filesList
                );

        for (String email : recipientEmails) {
            emailService.send(email, subject, body);
        }
    }
}
