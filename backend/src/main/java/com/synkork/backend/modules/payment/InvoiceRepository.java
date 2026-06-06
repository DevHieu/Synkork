package com.synkork.backend.modules.payment;

import com.synkork.backend.modules.payment.enums.InvoiceStatusEnum;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.UUID;

public interface InvoiceRepository extends JpaRepository<InvoiceEntity, UUID> {

    long countByStatus(InvoiceStatusEnum status);

    long countByStatusAndPaidAtBetween(InvoiceStatusEnum status, LocalDateTime start, LocalDateTime end);
}
