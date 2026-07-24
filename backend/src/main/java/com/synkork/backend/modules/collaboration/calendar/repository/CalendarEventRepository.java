package com.synkork.backend.modules.collaboration.calendar.repository;

import com.synkork.backend.modules.collaboration.calendar.entity.CalendarEventEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CalendarEventRepository extends JpaRepository<CalendarEventEntity, UUID> {

    @Override
    @EntityGraph(attributePaths = {"space", "space.room", "createdBy", "callRoomSpace", "attendees", "attendees.user"})
    Optional<CalendarEventEntity> findById(UUID id);

    @EntityGraph(attributePaths = {"space", "space.room", "createdBy", "callRoomSpace", "attendees", "attendees.user"})
    List<CalendarEventEntity> findBySpaceId(UUID spaceId);

    @EntityGraph(attributePaths = {"space", "space.room", "createdBy", "callRoomSpace", "attendees", "attendees.user"})
    List<CalendarEventEntity> findBySpaceIdAndEventDateBetween(UUID spaceId, LocalDate start, LocalDate end);

    @EntityGraph(attributePaths = {"space", "space.room", "createdBy", "callRoomSpace", "attendees", "attendees.user"})
    List<CalendarEventEntity> findBySpaceIdAndEventDateLessThanEqual(UUID spaceId, LocalDate endDate);

    @EntityGraph(attributePaths = {"space", "space.room", "createdBy", "callRoomSpace", "attendees", "attendees.user"})
    List<CalendarEventEntity> findBySpaceIdAndEventDate(UUID spaceId, LocalDate date);


    @EntityGraph(attributePaths = {"space", "space.room", "createdBy", "callRoomSpace", "attendees", "attendees.user"})
    @Query("SELECT e FROM CalendarEventEntity e WHERE e.eventDate >= :date OR (e.recurrenceType IS NOT NULL AND e.recurrenceType != 'NONE')")
    List<CalendarEventEntity> findUpcomingOrRecurringEvents(@Param("date") LocalDate date);

    List<CalendarEventEntity> findByCreatedByIdAndGoogleEventIdIsNull(UUID createdById);

    // pick group
    @EntityGraph(attributePaths = {"space", "createdBy", "callRoomSpace", "attendees", "attendees.user"})
    List<CalendarEventEntity> findByScheduleId(UUID scheduleId);

    void deleteByScheduleId(UUID scheduleId);

    void deleteBySpaceId(UUID spaceId);
}
