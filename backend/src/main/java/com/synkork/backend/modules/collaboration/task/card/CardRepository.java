package com.synkork.backend.modules.collaboration.task.card;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CardRepository extends JpaRepository<CardEntity, UUID> {
    Iterable<CardEntity> findByTitleContainingIgnoreCase(String title);
    
    List<CardEntity> findByColumn_IdOrderByPositionAsc(UUID columnId);
    List<CardEntity> findByAssigneeId(UUID userId);
    
}
