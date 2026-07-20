package com.synkork.backend.modules.payment.service;

import com.synkork.backend.modules.payment.dto.PlanPricingResponse;
import com.synkork.backend.modules.payment.dto.PlanPricingRequest;
import com.synkork.backend.modules.payment.entity.PlanPricingEntity;
import com.synkork.backend.modules.payment.enums.BillingCycleEnum;
import com.synkork.backend.modules.payment.repository.PlanPricingRepository;
import com.synkork.backend.modules.user.enums.PlanEnum;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PlanPricingService {

    @Autowired
    private PlanPricingRepository planPricingRepository;

    public List<PlanPricingResponse> getActivePricings() {
        return planPricingRepository.findByActiveTrue()
                .stream()
                .map(this::toDto)
                .toList();
    }

    public List<PlanPricingResponse> getPricingHistory(PlanEnum plan, BillingCycleEnum billingCycle) {
        return planPricingRepository.findByPlanAndBillingCycleOrderByCreatedAtDesc(plan, billingCycle)
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional
    public PlanPricingResponse changePrice(PlanPricingRequest request) {
        planPricingRepository
                .findByPlanAndBillingCycleAndActiveTrue(request.getPlan(), request.getBillingCycle())
                .ifPresent(oldPricing -> {
                    oldPricing.setActive(false);
                    planPricingRepository.save(oldPricing);
                });

        PlanPricingEntity newPricing = PlanPricingEntity.builder()
                .plan(request.getPlan())
                .billingCycle(request.getBillingCycle())
                .amount(request.getAmount())
                .active(true)
                .build();

        return toDto(planPricingRepository.save(newPricing));
    }

    private PlanPricingResponse toDto(PlanPricingEntity entity) {
        return PlanPricingResponse.builder()
                .id(entity.getId())
                .plan(entity.getPlan())
                .billingCycle(entity.getBillingCycle())
                .amount(entity.getAmount())
                .active(entity.isActive())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}