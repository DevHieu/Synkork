package com.synkork.backend.modules.collaboration.calendar.dto.googleCalendar;

import java.util.List;

public record GoogleCalendarEvent(
        String kind,
        String etag,
        String id,
        String status,
        String htmlLink,
        String created,
        String updated,
        String summary,
        String description,
        String location,
        GoogleCalendarPerson creator,
        GoogleCalendarPerson organizer,
        GoogleCalendarDateTime start,
        GoogleCalendarDateTime end,
        String iCalUID,
        Integer sequence,
        List<GoogleCalendarAttendee> attendees,
        Boolean guestsCanInviteOthers,
        Boolean privateCopy,
        GoogleCalendarReminders reminders,
        List<GoogleCalendarAttachment> attachments,
        String eventType
) {

    public record GoogleCalendarPerson(
            String email,
            Boolean self,
            String displayName
    ) {}

    public record GoogleCalendarDateTime(
            String dateTime,
            String date,
            String timeZone
    ) {}

    public record GoogleCalendarAttendee(
            String email,
            Boolean self,
            String responseStatus
    ) {}

    public record GoogleCalendarReminders(
            Boolean useDefault,
            List<GoogleCalendarReminderOverride> overrides
    ) {}

    public record GoogleCalendarReminderOverride(
            String method,
            Integer minutes
    ) {}

    public record GoogleCalendarAttachment(
            String fileUrl,
            String title,
            String iconLink
    ) {}
}