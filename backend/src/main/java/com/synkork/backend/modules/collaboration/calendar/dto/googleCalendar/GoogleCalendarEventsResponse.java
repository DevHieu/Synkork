package com.synkork.backend.modules.collaboration.calendar.dto.googleCalendar;

import java.util.List;

public record GoogleCalendarEventsResponse(
        String kind,
        String etag,
        String summary,
        String description,
        String updated,
        String timeZone,
        String accessRole,
        String nextSyncToken,
        List<GoogleCalendarEvent> items
) {
}