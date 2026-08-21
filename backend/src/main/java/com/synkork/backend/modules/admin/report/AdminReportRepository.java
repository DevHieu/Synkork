package com.synkork.backend.modules.admin.report;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.synkork.backend.modules.admin.statistics.dtos.ReportReasonStatsResponse;
import com.synkork.backend.modules.report.enums.ReportStatusEnums;
import com.synkork.backend.modules.report.enums.ReportTypeEnums;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.synkork.backend.modules.report.ReportEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AdminReportRepository extends JpaRepository<ReportEntity, UUID>, JpaSpecificationExecutor<ReportEntity>{
    long countByStatus(ReportStatusEnums status);
    long countByStatusAndCreatedAtBetween(ReportStatusEnums status, LocalDateTime from, LocalDateTime to);

    long countByReportType(ReportTypeEnums reportType);
    long countByCreatedAtBetween(LocalDateTime from, LocalDateTime to);

    long countByReportTypeAndCreatedAtBetween(
            ReportTypeEnums reportType,
            LocalDateTime from,
            LocalDateTime to
    );

    /**
     * Aggregate daily report counts by type within a date range.
     * Returns rows of [date, userCount, roomCount].
     */
    @Query("""
        SELECT
            CAST(r.createdAt AS LocalDate) AS date,
            SUM(CASE WHEN r.reportType = 'USER' THEN 1 ELSE 0 END) AS userReports,
            SUM(CASE WHEN r.reportType = 'ROOM' THEN 1 ELSE 0 END) AS roomReports
        FROM ReportEntity r
        WHERE (:from IS NULL OR r.createdAt >= :from)
          AND (:to IS NULL OR r.createdAt <= :to)
        GROUP BY CAST(r.createdAt AS LocalDate)
        ORDER BY CAST(r.createdAt AS LocalDate)
    """)
    List<Object[]> findDailyReportCounts(
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );

    @Query("""
        SELECT new com.synkork.backend.modules.admin.statistics.dtos.ReportReasonStatsResponse(
            r.reason,
            r.reportType,
            COUNT(r)
        )
        FROM ReportEntity r
        WHERE (:from IS NULL OR r.createdAt >= :from)
          AND (:to IS NULL OR r.createdAt <= :to)
        GROUP BY r.reason, r.reportType
        ORDER BY COUNT(r) DESC
    """)
    List<ReportReasonStatsResponse> findReasonCountsGroupedByType(
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );
}
