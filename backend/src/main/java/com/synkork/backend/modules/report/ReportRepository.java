package com.synkork.backend.modules.report;

import com.synkork.backend.modules.report.enums.ReportTypeEnums;
import com.synkork.backend.modules.room.RoomEntity;
import com.synkork.backend.modules.user.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ReportRepository extends JpaRepository<ReportEntity, UUID>, JpaSpecificationExecutor<ReportEntity> {

    boolean existsByReporterIdAndTargetUserAndReportType(UUID reporterId, UserEntity targetUser, ReportTypeEnums reportType);

    boolean existsByReporterIdAndTargetRoomAndReportType(UUID reporterId, RoomEntity targetRoom, ReportTypeEnums reportType);
}
