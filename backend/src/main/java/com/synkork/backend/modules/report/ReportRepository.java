package com.synkork.backend.modules.report;

import com.synkork.backend.modules.report.enums.ReportTypeEnums;
import com.synkork.backend.modules.room.RoomEntity;
import com.synkork.backend.modules.user.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ReportRepository extends JpaRepository<ReportEntity, UUID>, JpaSpecificationExecutor<ReportEntity> {

    boolean existsByReporterIdAndTargetUserAndReportType(UUID reporterId, UserEntity targetUser, ReportTypeEnums reportType);

    boolean existsByReporterIdAndTargetRoomAndReportType(UUID reporterId, RoomEntity targetRoom, ReportTypeEnums reportType);

    @Modifying
    @Query("""
        UPDATE ReportEntity r
        SET r.targetRoom = null
        WHERE r.targetRoom.id = :roomId
    """)
    int clearTargetRoom(@Param("roomId") UUID roomId);
}
