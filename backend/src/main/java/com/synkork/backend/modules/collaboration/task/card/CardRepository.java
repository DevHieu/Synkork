package com.synkork.backend.modules.collaboration.task.card;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CardRepository extends JpaRepository<CardEntity, UUID> {
    Iterable<CardEntity> findByTitleContainingIgnoreCase(String title);
    
    List<CardEntity> findByColumn_IdOrderByPositionAsc(UUID columnId);
    @Query("SELECT c FROM CardEntity c JOIN c.assignees u WHERE u.id = :userId")
    List<CardEntity> findByAssigneeId(@Param("userId") UUID userId);
    
    List<CardEntity> findByDueDateBeforeAndOverdueMailSentFalse(
        LocalDateTime time
    );

    @Query("""
    SELECT DISTINCT c
    FROM CardEntity c
    LEFT JOIN FETCH c.assignees
    WHERE c.dueDate IS NOT NULL
""")
List<CardEntity> findAllWithAssignees();
}
