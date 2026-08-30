package com.synkork.backend.modules.collaboration.calendar.utils;

import com.synkork.backend.modules.collaboration.calendar.dto.googleCalendar.GoogleCalendarEvent;
import com.synkork.backend.modules.collaboration.calendar.entity.CalendarEventEntity;
import com.synkork.backend.modules.collaboration.calendar.enums.SyncStatus;
import com.synkork.backend.modules.space.SpaceEntity;
import com.synkork.backend.modules.user.UserEntity;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;

@Component
public class GoogleCalendarUtils {
    public CalendarEventEntity mapGoogleEventToEntity(
            GoogleCalendarEvent googleEvent,
            UserEntity user,
            SpaceEntity personalCalendar
    ) {
        CalendarEventEntity entity = new CalendarEventEntity();

        entity.setCreatedBy(user);
        entity.setSpace(personalCalendar);

        entity.setGoogleEventId(googleEvent.id());
        entity.setGoogleCalendarId("primary");

        entity.setTitle(
                googleEvent.summary() != null
                        ? googleEvent.summary()
                        : "(Không có tiêu đề)"
        );

        entity.setDescription(
                googleEvent.description() != null
                        ? googleEvent.description()
                        : ""
        );

        GoogleCalendarEvent.GoogleCalendarDateTime start = googleEvent.start();
        GoogleCalendarEvent.GoogleCalendarDateTime end = googleEvent.end();

        // Google event có thể là event theo giờ hoặc all-day
        if (start != null) {
            if (start.dateTime() != null) {
                LocalDateTime startDateTime = OffsetDateTime.parse(start.dateTime())
                        .toLocalDateTime();

                entity.setEventDate(startDateTime.toLocalDate());
                entity.setStartTime(startDateTime.toLocalTime());
            } else if (start.date() != null) {
                LocalDate startDate = LocalDate.parse(start.date());

                entity.setEventDate(startDate);
                entity.setStartTime(LocalTime.MIN);
            }
        }

        if (end != null) {
            if (end.dateTime() != null) {
                LocalDateTime endDateTime = OffsetDateTime.parse(end.dateTime())
                        .toLocalDateTime();

                entity.setEndDate(endDateTime.toLocalDate());
                entity.setEndTime(endDateTime.toLocalTime());
            } else if (end.date() != null) {
                LocalDate endDate = LocalDate.parse(end.date());

                // Google all-day event: end.date là exclusive
                entity.setEndDate(endDate.minusDays(1));
                entity.setEndTime(LocalTime.MAX);
            }
        }

        entity.setSchedule(false);
        entity.setScheduleId(null);

        entity.setAllowEditAll(false);
        entity.setRemindBeforeMinutes(null);

        entity.setSyncStatus(SyncStatus.SUCCESS);
        entity.setLastSyncedAt(LocalDateTime.now());

        return entity;
    }
}
