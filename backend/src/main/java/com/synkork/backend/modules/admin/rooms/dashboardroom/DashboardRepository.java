package com.synkork.backend.modules.admin.rooms.dashboardroom;

import com.synkork.backend.modules.room.RoomEntity;
import com.synkork.backend.modules.room.enums.RoomStatusEnum;
import com.synkork.backend.modules.room.enums.RoomTypeEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface DashboardRepository extends JpaRepository<RoomEntity, UUID>, JpaSpecificationExecutor<RoomEntity> {

    // Stats
    long countByStatus(RoomStatusEnum status);
    long countByType(RoomTypeEnum type);
    long countByCreatedAtBetween(LocalDateTime from, LocalDateTime to);
    long countByStatusAndCreatedAtBetween(RoomStatusEnum status, LocalDateTime from, LocalDateTime to);
    long countByTypeAndCreatedAtBetween(RoomTypeEnum type, LocalDateTime from, LocalDateTime to);
    long countByWarningGreaterThan(int warning);
    long countByWarningGreaterThanAndCreatedAtBetween(int warning, LocalDateTime from, LocalDateTime to);

    @Query("""
            SELECT new com.synkork.backend.modules.admin.rooms.dashboardroom.RoomStatusCount(COUNT(r), r.status)
            FROM RoomEntity r
            WHERE (:start IS NULL OR r.createdAt >= :start)
              AND (:end IS NULL OR r.createdAt <= :end)
            GROUP BY r.status
            """)
    List<RoomStatusCount> countGroupByStatus(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    // Chart - cumulative counts up to a point in time
    long countByCreatedAtBefore(LocalDateTime before);
    long countByStatusAndCreatedAtBefore(RoomStatusEnum status, LocalDateTime before);
}
