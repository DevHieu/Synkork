package com.synkork.backend.modules.collaboration.calendar.utils;

import com.synkork.backend.modules.collaboration.calendar.dto.EventEmailInformation;
import com.synkork.backend.modules.collaboration.calendar.entity.CalendarEventEntity;
import com.synkork.backend.modules.collaboration.calendar.entity.EventAttachmentEntity;
import com.synkork.backend.modules.roomMember.RoomMemberEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class EventUtils {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    @Value("${frontend.client.url}")
    private String frontendUrl;

    public EventEmailInformation buildEventEmailInformation(CalendarEventEntity event, RoomMemberEntity attendee, boolean isReminder) {
        return EventEmailInformation.builder()
                .title(event.getTitle())
                .description(buildDescription(event))
                .roomName(event.getSpace().getRoom().getName())
                .spaceName(event.getSpace().getName())
                .creatorName(event.getCreatedBy() != null ? event.getCreatedBy().getDisplayName() : "Hệ thống")
                .timeStr(buildTimeStr(event))
                .recurrenceStr(buildRecurrenceStr(event))
                .taskInfo(buildTaskInfo(event))
                .noteInfo(buildNoteInfo(event))
                .callRoomUrl(buildCallRoomUrl(event))
                .callRoomName(event.getCallRoomSpace() != null ? event.getCallRoomSpace().getName() : null)
                .attendeeList(formatAttendeesList(event.getAttendees()))
                .fileList(formatFileList(event.getAttachments()))
                .totalAttendees(event.getAttendees().size())
                .recipientEmail(attendee.getUser().getEmail())
                .isReminder(isReminder)
                .build();
    }

    private String buildDescription(CalendarEventEntity event) {
        return (event.getDescription() != null && !event.getDescription().isBlank())
                ? event.getDescription()
                : "Không có mô tả";
    }

    private String buildTimeStr(CalendarEventEntity event) {
        String startTimeStr = event.getStartTime() != null ? event.getStartTime().format(TIME_FORMATTER) : "";
        String endTimeStr = event.getEndTime() != null ? event.getEndTime().format(TIME_FORMATTER) : "";
        String startDateStr = event.getEventDate() != null ? event.getEventDate().format(DATE_FORMATTER) : "";
        String endDateStr = event.getEndDate() != null ? event.getEndDate().format(DATE_FORMATTER) : startDateStr;

        return startDateStr.equals(endDateStr)
                ? String.format("%s - %s (%s)", startTimeStr, endTimeStr, startDateStr)
                : String.format("%s (%s) đến %s (%s)", startTimeStr, startDateStr, endTimeStr, endDateStr);
    }

    private String buildRecurrenceStr(CalendarEventEntity event) {
        if (event.getRecurrenceType() == null) return "Không lặp lại";

        return switch (event.getRecurrenceType()) {
            case "DAILY" -> "Hàng ngày";
            case "WEEKLY" -> "Hàng tuần";
            case "MONTHLY" -> "Hàng tháng";
            case "YEARLY" -> "Hàng năm";
            default -> event.getRecurrenceType().equalsIgnoreCase("NONE")
                    ? "Không lặp lại"
                    : event.getRecurrenceType();
        };
    }

    private String buildTaskInfo(CalendarEventEntity event) {
        if (event.getTask() == null) return null;

        String spaceId = event.getTask().getColumn() != null && event.getTask().getColumn().getSpace() != null
                ? event.getTask().getColumn().getSpace().getId().toString()
                : event.getSpace().getId().toString();
        String taskUrl = frontendUrl + "/spaces/" + spaceId + "?cardId=" + event.getTask().getId();

        return buildLink(taskUrl, event.getTask().getTitle());
    }

    private String buildNoteInfo(CalendarEventEntity event) {
        if (event.getNote() == null) return null;

        String noteSpaceId = event.getNote().getSpace() != null
                ? event.getNote().getSpace().getId().toString()
                : event.getSpace().getId().toString();
        String noteUrl = frontendUrl + "/spaces/" + noteSpaceId + "?noteId=" + event.getNote().getId();

        return buildLink(noteUrl, event.getNote().getTitle());
    }

    private String buildCallRoomUrl(CalendarEventEntity event) {
        return event.getCallRoomSpace() != null
                ? frontendUrl + "/spaces/" + event.getCallRoomSpace().getId()
                : null;
    }

    private String buildLink(String url, String label) {
        return "<a href='" + url + "' style='color: #023c3d; font-weight: bold; text-decoration: underline;'>" + label + "</a>";
    }

    private String formatAttendeesList(List<RoomMemberEntity> attendees) {
        return attendees.stream()
                .filter(a -> a.getUser() != null)
                .map(a -> a.getUser().getDisplayName() + " (" + a.getUser().getEmail() + ")")
                .collect(Collectors.joining("<br/>"));
    }

    private String formatFileList(List<EventAttachmentEntity> files) {
        return files != null && !files.isEmpty()
                ? files.stream().map(this::buildFileLink).collect(Collectors.joining())
                : "Không có";
    }

    private String buildFileLink(EventAttachmentEntity file) {
        return "<div style='margin-bottom: 6px;'>" + buildLink(file.getFileUrl(), file.getFileName()) + "</div>";
    }
}