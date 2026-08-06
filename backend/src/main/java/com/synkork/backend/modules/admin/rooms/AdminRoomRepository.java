package com.synkork.backend.modules.admin.rooms;

import com.synkork.backend.modules.admin.rooms.dtos.RoomStatusCount;
import com.synkork.backend.modules.room.RoomEntity;
import com.synkork.backend.modules.room.enums.RoomStatusEnum;
import com.synkork.backend.modules.room.enums.RoomTypeEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface AdminRoomRepository
        extends JpaRepository<RoomEntity, UUID>,
        JpaSpecificationExecutor<RoomEntity> {

    long countByTypeAndCreatedAtLessThan(RoomTypeEnum roomTypeEnum, LocalDateTime dateTo);
    long countByTypeAndCreatedAtBetween(RoomTypeEnum type, LocalDateTime from, LocalDateTime to);
    long countByWarningGreaterThanAndCreatedAtBetweenAndType(int warning, LocalDateTime from, LocalDateTime to, RoomTypeEnum type);

    @Query("""
            SELECT new com.synkork.backend.modules.admin.rooms.dtos.RoomStatusCount(COUNT(r), r.status)
            FROM RoomEntity r
            WHERE (:start IS NULL OR r.createdAt >= :start)
              AND (:end IS NULL OR r.createdAt <= :end)
              AND r.type = 'GROUP'
            GROUP BY r.status
            """)
    List<RoomStatusCount> countGroupByStatus(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    long countByType(RoomTypeEnum type);

    long countByCreatedAtBetweenAndType(LocalDateTime createdAtAfter, LocalDateTime createdAtBefore, RoomTypeEnum type);
}