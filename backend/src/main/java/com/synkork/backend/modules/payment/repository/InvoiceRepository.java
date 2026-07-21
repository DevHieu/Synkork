package com.synkork.backend.modules.payment.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.synkork.backend.modules.payment.entity.InvoiceEntity;
import com.synkork.backend.modules.payment.enums.InvoiceStatusEnum;

@Repository
public interface InvoiceRepository extends JpaRepository<InvoiceEntity, UUID>, JpaSpecificationExecutor<InvoiceEntity> {

    long countByStatus(InvoiceStatusEnum status);

    long countByStatusAndPaidAtBetween(InvoiceStatusEnum status, LocalDateTime start, LocalDateTime end);

    Optional<InvoiceEntity> findByTransactionId(String transactionId);

    @Query("SELECT COALESCE(SUM(i.amount), 0) FROM InvoiceEntity i WHERE i.status = :status")
    BigDecimal sumAmountByStatus(@Param("status") InvoiceStatusEnum status);

    @Query("SELECT COALESCE(SUM(i.amount), 0) FROM InvoiceEntity i WHERE i.status = :status AND i.paidAt >= :start")
    BigDecimal sumAmountByStatusAndPaidAtAfter(@Param("status") InvoiceStatusEnum status, @Param("start") LocalDateTime start);
}