package com.synkork.backend.modules.statistics;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface StatisticsRepository extends JpaRepository<StatisticsEntity, UUID> {
    @Query("SELECT s FROM StatisticsEntity s WHERE s.createdAt >= :from ORDER BY s.createdAt")
    List<StatisticsEntity> findByDateRange(@Param("from") LocalDateTime from);

    @Query("SELECT s FROM StatisticsEntity s WHERE DATE(s.createdAt) = DATE(:date)")
    Optional<StatisticsEntity> findByDate(@Param("date") LocalDateTime date);
}
