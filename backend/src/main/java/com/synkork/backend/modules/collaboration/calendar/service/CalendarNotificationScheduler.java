package com.synkork.backend.modules.collaboration.calendar.service;

import com.synkork.backend.modules.collaboration.calendar.entity.CalendarEventEntity;
import com.synkork.backend.modules.collaboration.calendar.repository.CalendarEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.time.temporal.ChronoUnit;

@Component
@RequiredArgsConstructor
public class CalendarNotificationScheduler {

    private final CalendarEventRepository calendarEventRepository;
    private final CalendarEmailService calendarEmailService;


    @Scheduled(cron = "0 * * * * *")
    @Transactional(readOnly = true)
    public void scanAndRemindUpcomingEvents() {
        LocalDateTime now = LocalDateTime.now().withSecond(0).withNano(0);
        List<CalendarEventEntity> events = calendarEventRepository.findUpcomingOrRecurringEvents(now.toLocalDate());
        
        Set<Long> notifyMinutes = Set.of(
            21 * 24 * 60L, // 3 weeks
            7 * 24 * 60L,  // 1 week
            4 * 24 * 60L,  // 4 days
            2 * 24 * 60L,  // 2 days
            24 * 60L,      // 1 day
            16 * 60L,      // 16 hours
            8 * 60L,       // 8 hours
            4 * 60L,       // 4 hours
            2 * 60L,       // 2 hours
            60L,           // 1 hour
            30L,           // 30 minutes
            15L            // 15 minutes
        );

        for (CalendarEventEntity event : events) {
            LocalDateTime eventTime = LocalDateTime.of(event.getEventDate(), event.getStartTime()).withSecond(0).withNano(0);
            
            if (event.getRecurrenceType() != null && !event.getRecurrenceType().equals("NONE")) {
                eventTime = findNextOccurrence(event, now);
            }
            
            if (eventTime == null || eventTime.isBefore(now)) continue;

            long diffMinutes = ChronoUnit.MINUTES.between(now, eventTime);
            
            boolean shouldNotify = notifyMinutes.contains(diffMinutes);
            if (event.getRemindBeforeMinutes() != null && diffMinutes == event.getRemindBeforeMinutes().longValue()) {
                shouldNotify = true;
            }

            if (shouldNotify && !event.getAttendees().isEmpty()) {
                calendarEmailService.sendEventNotificationEmail(event, event.getAttendees(), true);
            }
        }
    }
    
    private LocalDateTime findNextOccurrence(CalendarEventEntity event, LocalDateTime now) {
        LocalDate current = event.getEventDate();
        LocalDate limit = event.getRecurrenceEndDate() != null ? event.getRecurrenceEndDate() : current.plusYears(1);
        
        while (!current.isAfter(limit)) {
            LocalDateTime dt = LocalDateTime.of(current, event.getStartTime());
            if (!dt.isBefore(now)) {
                return dt;
            }
            current = getNextDate(current, event.getRecurrenceType());
            if (current == null) return null;
        }
        return null;
    }
    
    private LocalDate getNextDate(LocalDate current, String type) {
        if ("DAILY".equals(type)) return current.plusDays(1);
        if ("WEEKLY".equals(type)) return current.plusWeeks(1);
        if ("MONTHLY".equals(type)) return current.plusMonths(1);
        if ("YEARLY".equals(type)) return current.plusYears(1);
        return null;
    }
}
