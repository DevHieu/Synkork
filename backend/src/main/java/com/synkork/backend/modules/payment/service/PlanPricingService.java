package com.synkork.backend.modules.payment.service;

import com.synkork.backend.modules.payment.dto.PlanPricingResponse;
import com.synkork.backend.modules.payment.dto.PlanPricingRequest;
import com.synkork.backend.modules.payment.entity.PlanPricingEntity;
import com.synkork.backend.modules.payment.enums.BillingCycleEnum;
import com.synkork.backend.modules.payment.enums.DiscountTypeEnum;
import com.synkork.backend.modules.payment.repository.PlanPricingRepository;
import com.synkork.backend.modules.user.enums.PlanEnum;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
        validateDiscount(request);
        BigDecimal discountAmount = calculateDiscountAmount(
                request.getAmount(),
                request.getDiscountType(),
                request.getDiscountValue()
        );

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
                .discountType(request.getDiscountType())
                .discountValue(request.getDiscountType() == null ? null : request.getDiscountValue())
                .discountAmount(discountAmount)
                .active(true)
                .build();

        return toDto(planPricingRepository.save(newPricing));
    }

    private void validateDiscount(PlanPricingRequest request) {
        DiscountTypeEnum discountType = request.getDiscountType();
        BigDecimal discountValue = request.getDiscountValue();

        if (discountType == null) {
            if (discountValue != null && discountValue.compareTo(BigDecimal.ZERO) > 0) {
                throw new IllegalArgumentException("discountType không được để trống khi có discountValue");
            }
            return;
        }

        if (discountValue == null) {
            throw new IllegalArgumentException("discountValue không được để trống khi có discountType");
        }

        if (discountType == DiscountTypeEnum.PERCENTAGE
                && discountValue.compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new IllegalArgumentException("discountValue không được vượt quá 100 khi giảm theo phần trăm");
        }
    }

    private BigDecimal calculateDiscountAmount(BigDecimal amount, DiscountTypeEnum discountType, BigDecimal discountValue) {
        if (discountType == null || discountValue == null) {
            return BigDecimal.ZERO;
        }

        BigDecimal discountAmount = discountType == DiscountTypeEnum.PERCENTAGE
                ? amount.multiply(discountValue).divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP)
                : discountValue;

        return discountAmount.min(amount).max(BigDecimal.ZERO);
    }

    private PlanPricingResponse toDto(PlanPricingEntity entity) {
        BigDecimal discountAmount = entity.getDiscountAmount() != null
                ? entity.getDiscountAmount()
                : calculateDiscountAmount(entity.getAmount(), entity.getDiscountType(), entity.getDiscountValue());

        return PlanPricingResponse.builder()
                .id(entity.getId())
                .plan(entity.getPlan())
                .billingCycle(entity.getBillingCycle())
                .amount(entity.getAmount())
                .discountType(entity.getDiscountType())
                .discountValue(entity.getDiscountValue())
                .discountAmount(discountAmount)
                .finalAmount(entity.getAmount().subtract(discountAmount).max(BigDecimal.ZERO))
                .active(entity.isActive())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
