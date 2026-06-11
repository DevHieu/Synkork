package com.synkork.backend.modules.payment;

import com.synkork.backend.modules.payment.enums.InvoiceStatusEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InvoiceRepository extends JpaRepository<InvoiceEntity, UUID>, JpaSpecificationExecutor<InvoiceEntity> {

    long countByStatus(InvoiceStatusEnum status);

    long countByStatusAndPaidAtBetween(InvoiceStatusEnum status, LocalDateTime start, LocalDateTime end);

    Optional<InvoiceEntity> findByTransactionId(String transactionId);
}
