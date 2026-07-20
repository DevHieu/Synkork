package com.synkork.backend.modules.payment.repository;

import com.synkork.backend.modules.payment.entity.PlanPricingEntity;
import com.synkork.backend.modules.payment.enums.BillingCycleEnum;
import com.synkork.backend.modules.user.enums.PlanEnum;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PlanPricingRepository extends JpaRepository<PlanPricingEntity, UUID> {

    Optional<PlanPricingEntity> findByPlanAndBillingCycleAndActiveTrue(PlanEnum plan, BillingCycleEnum billingCycle);

    List<PlanPricingEntity> findByActiveTrue();

    List<PlanPricingEntity> findByPlanAndBillingCycleOrderByCreatedAtDesc(PlanEnum plan, BillingCycleEnum billingCycle);

}
   
