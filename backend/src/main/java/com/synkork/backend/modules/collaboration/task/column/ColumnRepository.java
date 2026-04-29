package com.synkork.backend.modules.collaboration.task.column;

import java.util.List;
import java.util.UUID;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ColumnRepository extends JpaRepository<ColumnEntity, UUID> {
    // Tìm các cột của 1 board và sắp xếp theo vị trí
    @EntityGraph(attributePaths = {
            "cards"
    })
    List<ColumnEntity> findBySpaceIdOrderByPositionAsc(UUID spaceId);

    void deleteBySpaceId(UUID spaceId);
}
