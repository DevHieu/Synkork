package com.synkork.backend.modules.payment.repository;

import com.synkork.backend.modules.payment.entity.UserSubscriptionEntity;
import com.synkork.backend.modules.user.enums.PlanEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserSubscriptionRepository 
        extends JpaRepository<UserSubscriptionEntity, UUID>, JpaSpecificationExecutor<UserSubscriptionEntity> {

    Optional<UserSubscriptionEntity> findByUserIdAndCurrentTrue(UUID userId);

    Optional<UserSubscriptionEntity> findByInvoiceId(UUID invoiceId);

    long countByCurrentTrue();

    long countByCurrentTrueAndStartedAtBetween(LocalDateTime dateFrom, LocalDateTime dateTo);

    long countByCurrentTrueAndStartedAtLessThanEqual(LocalDateTime dateTo);

    long countByPlan(PlanEnum plan);

    long countByPlanAndStartedAtBetween(PlanEnum plan, LocalDateTime start, LocalDateTime end);


    @Query("""
            SELECT COUNT(subscription)
            FROM UserSubscriptionEntity subscription
            WHERE subscription.plan IN :plans
              AND (:start IS NULL OR subscription.startedAt >= :start)
              AND (:end IS NULL OR subscription.startedAt <= :end)
            """)
    long countByPlanIn(
            @Param("plans") Collection<PlanEnum> plans,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query("""
            SELECT COUNT(subscription)
            FROM UserSubscriptionEntity subscription
            WHERE subscription.plan IN :plans
              AND (:start IS NULL OR subscription.startedAt >= :start)
              AND (:end IS NULL OR subscription.startedAt <= :end)
              AND EXISTS (
                SELECT 1
                FROM UserSubscriptionEntity priorSubscription
                WHERE priorSubscription.user = subscription.user
                  AND priorSubscription.plan IN :plans
                  AND priorSubscription.startedAt < subscription.startedAt
              )
            """)
    long countRenewedPaidSubscriptions(
            @Param("plans") Collection<PlanEnum> plans,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

}
