package com.synkork.backend.modules.collaboration.calendar.dto.googleCalendar;

import java.util.UUID;

public class CalendarEventSyncRequestedEvent {
    private final UUID eventId;
    public CalendarEventSyncRequestedEvent(UUID eventId) { this.eventId = eventId; }
    public UUID getEventId() { return eventId; }
}
