package com.synkork.backend.modules.collaboration.task.board;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BoardRepository extends JpaRepository<BoardEntity, UUID> {

    @EntityGraph(attributePaths = {
            "columns",
            "columns.cards"
    })
    Optional<BoardEntity> findByIdAndSpaceId(UUID id, UUID spaceId);
}
