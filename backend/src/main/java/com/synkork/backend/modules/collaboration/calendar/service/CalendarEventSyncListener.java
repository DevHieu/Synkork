package com.synkork.backend.modules.collaboration.calendar.service;

import com.synkork.backend.modules.collaboration.calendar.dto.googleCalendar.CalendarEventSyncRequestedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class CalendarEventSyncListener {

    private final GoogleCalendarService googleCalendarService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onSyncRequested(CalendarEventSyncRequestedEvent event) {
        googleCalendarService.syncEventToGoogle(event.getEventId());
    }
}