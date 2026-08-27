package com.synkork.backend.modules.payment.service;

import com.synkork.backend.modules.payment.entity.InvoiceEntity;
import com.synkork.backend.modules.payment.entity.UserSubscriptionEntity;
import com.synkork.backend.modules.payment.enums.SubscriptionStatusEnum;
import com.synkork.backend.modules.payment.repository.UserSubscriptionRepository;
import com.synkork.backend.modules.user.UserEntity;
import com.synkork.backend.modules.user.UserService;
import com.synkork.backend.modules.user.enums.PlanEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class PaymentService {

    @Autowired
    private UserSubscriptionRepository userSubscriptionRepository;

    @Autowired
    private UserService userService;

    private void deactivateCurrentSubscription(UserEntity user) {
        userSubscriptionRepository.findByUserIdAndCurrentTrue(user.getId())
                .ifPresent(oldSubscription -> {
                    oldSubscription.setCurrent(false);
                    oldSubscription.setStatus(SubscriptionStatusEnum.EXPIRED);
                    userSubscriptionRepository.save(oldSubscription);
                });
    }

    public void createNewSubscription(UserEntity user, String plan, InvoiceEntity invoice,
                                      LocalDateTime now, LocalDateTime expireDate) {

        this.deactivateCurrentSubscription(user);

        UserSubscriptionEntity subscription = UserSubscriptionEntity.builder()
                .user(user)
                .plan(PlanEnum.valueOf(plan.toUpperCase()))
                .status(SubscriptionStatusEnum.ACTIVE)
                .startedAt(now)
                .expiresAt(expireDate)
                .autoRenew(false)
                .invoice(invoice)
                .current(true)
                .build();

        userSubscriptionRepository.save(subscription);
    }

    public void updateUserPlanCache(UserEntity user, String plan, LocalDateTime expireDate) {
        user.setCurrentPlan(PlanEnum.valueOf(plan.toUpperCase()));
        user.setPlanExpiresAt(expireDate);
        userService.create(user);
    }
}
