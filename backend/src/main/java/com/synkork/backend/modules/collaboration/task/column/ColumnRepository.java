package com.synkork.backend.modules.collaboration.task.column;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.transaction.Transactional;

@Repository
public interface ColumnRepository extends JpaRepository<ColumnEntity, UUID> {
    // Tìm các cột của 1 board và sắp xếp theo vị trí
    @EntityGraph(attributePaths = {
            "cards"
    })
    List<ColumnEntity> findBySpaceIdOrderByPositionAsc(UUID spaceId);

    @Query("SELECT c FROM ColumnEntity c WHERE c.space.id = :spaceId ORDER BY c.position ASC")
    List<ColumnEntity> findColumnsOnlyBySpaceId(@Param("spaceId") UUID spaceId);

    void deleteBySpaceId(UUID spaceId);

    
    List<ColumnEntity> findBySpaceIdAndArchivedFalseOrderByPositionAsc(UUID spaceId);

    List<ColumnEntity> findBySpaceIdAndArchivedTrueOrderByPositionAsc(UUID spaceId);

    @Transactional
    @Modifying
    @Query("""
                            DELETE FROM ColumnEntity c
                            WHERE c.archived = true AND c.space.id = :spaceId
                        """)
    int deleteAllArchivedColumns(@Param("spaceId") UUID spaceId);
}
