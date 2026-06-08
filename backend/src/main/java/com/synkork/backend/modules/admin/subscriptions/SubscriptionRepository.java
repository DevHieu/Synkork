package com.synkork.backend.modules.admin.subscriptions;

import com.synkork.backend.modules.admin.subscriptions.dto.BillingDTO;
import com.synkork.backend.modules.payment.enums.InvoiceStatusEnum;
import com.synkork.backend.modules.payment.enums.PaymentMethodEnum;
import com.synkork.backend.modules.user.enums.PlanEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SubscriptionRepository extends JpaRepository<SubscriptionEntity, UUID> {

    @Query("SELECT new com.synkork.backend.modules.admin.subscriptions.dto.BillingDTO(" +
           "s.id, s.amount, s.paidAt, s.createdAt, s.updatedAt, " +
           "s.user.currentPlan, s.invoiceStatus, s.transactionId, " +
           "s.paymentMethodEnum, s.user.email, s.user.username) " +
           "FROM SubscriptionEntity s " +
           "WHERE (:status IS NULL OR s.invoiceStatus = :status) " +
           "AND (:plan IS NULL OR s.user.currentPlan = :plan) " +
           "AND (:paymentMethod IS NULL OR s.paymentMethodEnum = :paymentMethod) " +
           "AND (:email IS NULL OR LOWER(s.user.email) LIKE LOWER(CONCAT('%', :email, '%'))) " +
           "AND (:username IS NULL OR LOWER(s.user.username) LIKE LOWER(CONCAT('%', :username, '%'))) " +
           "AND (:startDate IS NULL OR s.createdAt >= :startDate) " +
           "AND (:endDate IS NULL OR s.createdAt <= :endDate)")
    Page<BillingDTO> findAllBillings(
            @Param("status") InvoiceStatusEnum status,
            @Param("plan") PlanEnum plan,
            @Param("paymentMethod") PaymentMethodEnum paymentMethod,
            @Param("email") String email,
            @Param("username") String username,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable
    );

    @Query("SELECT new com.synkork.backend.modules.admin.subscriptions.dto.BillingDTO(" +
           "s.id, s.amount, s.paidAt, s.createdAt, s.updatedAt, " +
           "s.user.currentPlan, s.invoiceStatus, s.transactionId, " +
           "s.paymentMethodEnum, s.user.email, s.user.username) " +
           "FROM SubscriptionEntity s WHERE s.id = :id")
    Optional<BillingDTO> findBillingById(@Param("id") UUID id);
}
