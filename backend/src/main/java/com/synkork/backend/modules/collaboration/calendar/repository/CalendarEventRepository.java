package com.synkork.backend.modules.collaboration.calendar.repository;

import com.synkork.backend.modules.collaboration.calendar.entity.CalendarEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface CalendarEventRepository extends JpaRepository<CalendarEventEntity, UUID> {

    List<CalendarEventEntity> findBySpaceId(UUID spaceId);

    @Query("""
            select event from CalendarEventEntity event
            where event.space.id = :spaceId
              and event.eventDate <= :end
              and coalesce(event.endDate, event.eventDate) >= :start
            """)
    List<CalendarEventEntity> findOverlappingDateRange(
            @Param("spaceId") UUID spaceId,
            @Param("start") LocalDate start,
            @Param("end") LocalDate end
    );

    @Query("""
            select event from CalendarEventEntity event
            where event.space.id = :spaceId
              and event.eventDate <= :endDate
              and event.recurrenceType is not null
              and event.recurrenceType <> 'NONE'
            """)
    List<CalendarEventEntity> findRecurringBySpaceIdStartingBeforeOrOn(
            @Param("spaceId") UUID spaceId,
            @Param("endDate") LocalDate endDate
    );

    List<CalendarEventEntity> findBySpaceIdAndEventDate(UUID spaceId, LocalDate date);

    void deleteBySpaceId(UUID spaceId);
}
