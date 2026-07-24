package com.synkork.backend.modules.admin.subscriptions.service;

import com.synkork.backend.modules.admin.subscriptions.specification.AdminSubscriptionSpecification;
import com.synkork.backend.modules.admin.subscriptions.dtos.AdminSubscriptionFilterRequest;
import com.synkork.backend.modules.admin.subscriptions.dtos.AdminSubscriptionResponse;
import com.synkork.backend.modules.payment.entity.UserSubscriptionEntity;
import com.synkork.backend.modules.payment.repository.UserSubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminSubscriptionService {
    private final UserSubscriptionRepository userSubscriptionRepository;

    public Page<AdminSubscriptionResponse> getSubscriptions(AdminSubscriptionFilterRequest request) {
        request.validate();

        Pageable pageable = PageRequest.of(
                request.getPage(),
                request.getSize(),
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        Specification<UserSubscriptionEntity> specification = AdminSubscriptionSpecification.filter(request);

        return userSubscriptionRepository.findAll(specification, pageable)
                .map(AdminSubscriptionResponse::from);
    }

    public AdminSubscriptionResponse getSubscriptionById(UUID id) {
        return AdminSubscriptionResponse.from(
                userSubscriptionRepository.findById(id)
                        .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy gói đăng ký: " + id))
        );
    }
}
