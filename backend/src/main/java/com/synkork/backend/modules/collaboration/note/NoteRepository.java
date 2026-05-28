package com.synkork.backend.modules.collaboration.note;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface NoteRepository extends JpaRepository<NoteEntity, UUID> {

    List<NoteEntity> findBySpaceId(UUID spaceUuid);

    List<NoteEntity> findByTitleContainingIgnoreCase(String title);

    @Query("""
        SELECT n
        FROM NoteEntity n
        JOIN FETCH n.space s
        JOIN FETCH s.room r
        WHERE n.reminderAt <= :now
        AND n.reminderSent = false
        AND n.reminderAt IS NOT NULL
    """)
    List<NoteEntity> findPendingReminders(@Param("now") Instant now);
}