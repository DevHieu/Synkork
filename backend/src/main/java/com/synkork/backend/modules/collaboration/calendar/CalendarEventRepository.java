package com.synkork.backend.modules.collaboration.calendar;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface CalendarEventRepository extends JpaRepository<CalendarEventEntity, UUID> {

    List<CalendarEventEntity> findBySpaceId(UUID spaceId);

    List<CalendarEventEntity> findBySpaceIdAndEventDateBetween(UUID spaceId, LocalDate start, LocalDate end);

    List<CalendarEventEntity> findBySpaceIdAndEventDate(UUID spaceId, LocalDate date);

    void deleteBySpaceId(UUID spaceId);
}
