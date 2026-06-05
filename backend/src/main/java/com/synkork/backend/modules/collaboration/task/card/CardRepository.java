package com.synkork.backend.modules.collaboration.task.card;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.transaction.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface CardRepository extends JpaRepository<CardEntity, UUID> {
      Iterable<CardEntity> findByTitleContainingIgnoreCase(String title);

      List<CardEntity> findByColumn_IdOrderByPositionAsc(UUID columnId);

      @Query("SELECT c FROM CardEntity c JOIN c.assignees u WHERE u.id = :userId")
      List<CardEntity> findByAssigneeId(@Param("userId") UUID userId);

      List<CardEntity> findByDueDateBeforeAndOverdueMailSentFalse(
                  LocalDateTime time);

      @Query("SELECT c FROM CardEntity c " +
                  "JOIN FETCH c.assignees a " +
                  "JOIN FETCH a.user " +
                  "JOIN FETCH c.column col " +
                  "JOIN FETCH col.space s " +
                  "JOIN FETCH s.room " +
                  "WHERE c.dueDate IS NOT NULL")
      List<CardEntity> findAllWithAssignees();

      @Query("""
                        SELECT c.column.space.room.id
                        FROM CardEntity c
                        WHERE c.id = :cardId
                  """)
      UUID findRoomIdByCardId(@Param("cardId") UUID cardId);

      List<CardEntity> findByColumn_IdAndArchivedFalseOrderByPositionAsc(UUID columnId);

      List<CardEntity> findByColumn_IdAndArchivedTrueOrderByPositionAsc(UUID columnId);

      @Query("""
                  SELECT c FROM CardEntity c
                  JOIN c.column col
                  JOIN col.space s
                  WHERE s.id = :spaceId AND c.archived = true
                  ORDER BY c.archivedAt DESC
                  """)
      List<CardEntity> findArchivedCardsBySpaceId(@Param("spaceId") UUID spaceId);

      @Transactional
      @Modifying
      @Query("""
                        DELETE FROM CardEntity c
                        WHERE c.archived = true
                        AND c.column.id IN (
                              SELECT col.id
                              FROM ColumnEntity col
                              WHERE col.space.id = :spaceId
                        )
                  """)
      int deleteAllArchivedCards(@Param("spaceId") UUID spaceId);
}
