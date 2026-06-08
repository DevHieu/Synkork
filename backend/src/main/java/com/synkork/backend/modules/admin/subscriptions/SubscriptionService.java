package com.synkork.backend.modules.admin.subscriptions;

import com.synkork.backend.modules.admin.subscriptions.dto.BillingDTO;
import com.synkork.backend.modules.admin.subscriptions.dto.BillingRequestDTO;
import com.synkork.backend.modules.payment.ExpiredSubscriptionService;
import com.synkork.backend.modules.payment.enums.InvoiceStatusEnum;
import com.synkork.backend.modules.payment.enums.PaymentMethodEnum;
import com.synkork.backend.modules.user.UserEntity;
import com.synkork.backend.modules.user.UserRepository;
import com.synkork.backend.modules.user.enums.PlanEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class SubscriptionService {

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ExpiredSubscriptionService expiredSubscriptionService;

    public Page<BillingDTO> getBillings(
            InvoiceStatusEnum status,
            PlanEnum plan,
            PaymentMethodEnum paymentMethod,
            String email,
            String username,
            LocalDateTime startDate,
            LocalDateTime endDate,
            int page,
            int size
    ) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.max(size, 1), Sort.by(Sort.Direction.DESC, "createdAt"));
        return subscriptionRepository.findAllBillings(status, plan, paymentMethod, email, username, startDate, endDate, pageable);
    }

    public BillingDTO getBillingById(UUID id) {
        return subscriptionRepository.findBillingById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy hóa đơn"));
    }

    public BillingDTO createBilling(BillingRequestDTO request) {
        UserEntity user = userRepository.findByEmail(request.getUserEmail())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng với email: " + request.getUserEmail()));

        SubscriptionEntity subscription = subscriptionRepository.save(SubscriptionEntity.from(user, request));

        if (request.getStatusEnum() == InvoiceStatusEnum.PAID) {
            updateUserPlan(user, request.getPlanEnum());
        }

        return getBillingById(subscription.getId());
    }

    public BillingDTO updateBilling(UUID id, BillingRequestDTO request) {
        SubscriptionEntity subscription = subscriptionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy hóa đơn"));

        if (request.getUserEmail() != null) {
            UserEntity user = userRepository.findByEmail(request.getUserEmail())
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng với email: " + request.getUserEmail()));
            subscription.setUser(user);
        }

        if (request.getAmount() != null) {
            subscription.setAmount(request.getAmount());
        }

        if (request.getStatus() != null) {
            InvoiceStatusEnum newStatus = request.getStatusEnum();
            if (newStatus == InvoiceStatusEnum.PAID && subscription.getInvoiceStatus() != InvoiceStatusEnum.PAID) {
                subscription.setPaidAt(LocalDateTime.now());
                updateUserPlan(subscription.getUser(), request.getPlan() != null ? request.getPlanEnum() : subscription.getUser().getCurrentPlan());
            }
            subscription.setInvoiceStatus(newStatus);
        }

        if (request.getPaymentMethod() != null) {
            subscription.setPaymentMethodEnum(request.getPaymentMethodEnum());
        }

        if (request.getOrderId() != null) {
            subscription.setTransactionId(request.getOrderId());
        }

        subscriptionRepository.save(subscription);
        return getBillingById(id);
    }

    public void deleteBilling(UUID id) {
        SubscriptionEntity subscription = subscriptionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy hóa đơn"));
        subscriptionRepository.delete(subscription);
    }

    private void updateUserPlan(UserEntity user, PlanEnum plan) {
        user.setCurrentPlan(plan);
        user.setPlanExpiresAt(plan == PlanEnum.FREE ? null : LocalDateTime.now().plusMonths(1).plusDays(3));
        userRepository.save(user);
        try {
            expiredSubscriptionService.changePendingRoomAndSpace(user.getId());
        } catch (Exception ignored) {}
    }
}
