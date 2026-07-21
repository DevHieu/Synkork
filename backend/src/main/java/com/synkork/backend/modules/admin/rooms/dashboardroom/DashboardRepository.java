package com.synkork.backend.modules.admin.rooms.dashboardroom;

import com.synkork.backend.modules.room.RoomEntity;
import com.synkork.backend.modules.room.enums.RoomStatusEnum;
import com.synkork.backend.modules.room.enums.RoomTypeEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.UUID;

@Repository
public interface DashboardRepository extends JpaRepository<RoomEntity, UUID>, JpaSpecificationExecutor<RoomEntity> {

    // Stats
    long countByStatus(RoomStatusEnum status);
    long countByType(RoomTypeEnum type);
    long countByCreatedAtBetween(LocalDateTime from, LocalDateTime to);
    long countByStatusAndCreatedAtBetween(RoomStatusEnum status, LocalDateTime from, LocalDateTime to);
    long countByTypeAndCreatedAtBetween(RoomTypeEnum type, LocalDateTime from, LocalDateTime to);

    // Chart - cumulative counts up to a point in time
    long countByCreatedAtBefore(LocalDateTime before);
    long countByStatusAndCreatedAtBefore(RoomStatusEnum status, LocalDateTime before);
}
