package com.synkork.backend.modules.collaboration.calendar.dto;

import lombok.Builder;

@Builder()
public record EventEmailInformation(
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
        String attendeeList,
        String fileList,
        int totalAttendees,
        String recipientEmail,
        boolean isReminder
) {
}