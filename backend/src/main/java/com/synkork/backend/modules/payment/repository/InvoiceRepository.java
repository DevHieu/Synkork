package com.synkork.backend.modules.payment.repository;

import com.synkork.backend.modules.admin.statistics.dtos.InvoiceStatusCount;
import com.synkork.backend.modules.payment.entity.InvoiceEntity;
import com.synkork.backend.modules.payment.enums.InvoiceStatusEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Pageable;

@Repository
public interface InvoiceRepository extends JpaRepository<InvoiceEntity, UUID>, JpaSpecificationExecutor<InvoiceEntity> {

    long countByStatus(InvoiceStatusEnum status);

    long countByStatusAndCreatedAtBetween(InvoiceStatusEnum status, LocalDateTime from, LocalDateTime to);

    long countByStatusAndPaidAtBetween(InvoiceStatusEnum status, LocalDateTime start, LocalDateTime end);

    @Query("SELECT COALESCE(SUM(i.amount), 0) FROM InvoiceEntity i WHERE i.status = :status AND (:start IS NULL OR i.paidAt >= :start) AND (:end IS NULL OR i.paidAt <= :end)")
    BigDecimal sumAmountByStatus(
            @Param("status") InvoiceStatusEnum status,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
        );

    @Query("""
    SELECT new com.synkork.backend.modules.admin.statistics.dtos.InvoiceStatusCount(COUNT(i), i.status)
    FROM InvoiceEntity i
    WHERE (:start IS NULL OR i.createdAt >= :start)
      AND (:end IS NULL OR i.createdAt <= :end)
    GROUP BY i.status
    """)
    List<InvoiceStatusCount> countGroupByStatus(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}
