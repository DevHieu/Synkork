package com.synkork.backend.modules.admin.subscriptions.service;

import com.synkork.backend.modules.admin.statistics.dtos.InvoiceStatusCount;
import com.synkork.backend.modules.admin.statistics.dtos.SubscriptionDashboardResponse;
import com.synkork.backend.modules.admin.subscriptions.dtos.SubscriptionDashboardChart;
import com.synkork.backend.modules.admin.subscriptions.specification.AdminSubscriptionSpecification;
import com.synkork.backend.modules.admin.subscriptions.dtos.AdminSubscriptionFilterRequest;
import com.synkork.backend.modules.admin.subscriptions.dtos.AdminSubscriptionResponse;
import com.synkork.backend.modules.admin.utils.AdminUtils;
import com.synkork.backend.modules.payment.entity.UserSubscriptionEntity;
import com.synkork.backend.modules.payment.enums.InvoiceStatusEnum;
import com.synkork.backend.modules.payment.repository.InvoiceRepository;
import com.synkork.backend.modules.payment.repository.UserSubscriptionRepository;
import com.synkork.backend.modules.user.enums.PlanEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminSubscriptionService {
    private final UserSubscriptionRepository userSubscriptionRepository;
    private final InvoiceRepository invoiceRepository;

    private static final List<PlanEnum> PAID_PLANS = List.of(PlanEnum.TEAM, PlanEnum.BUSINESS);

    public SubscriptionDashboardResponse getSubscriptionDashboardData(LocalDateTime dateFrom, LocalDateTime dateTo) {
        BigDecimal totalRevenue = invoiceRepository.sumAmountByStatus(InvoiceStatusEnum.PAID, dateFrom, dateTo);

        long newSubscriptions = userSubscriptionRepository.countByPlanIn(PAID_PLANS, dateFrom, dateTo);
        long renewedSubscriptions = userSubscriptionRepository.countRenewedPaidSubscriptions(PAID_PLANS, dateFrom, dateTo);
        double renewalRate = AdminUtils.calcRate(renewedSubscriptions, newSubscriptions);

        List<InvoiceStatusCount> counts = invoiceRepository.countGroupByStatus(dateFrom, dateTo);
        Map<InvoiceStatusEnum, Long> statusMap = counts.stream()
                .collect(Collectors.toMap(InvoiceStatusCount::status, InvoiceStatusCount::count));

        long pendingInvoices = statusMap.getOrDefault(InvoiceStatusEnum.PENDING, 0L);
        long paidInvoices = statusMap.getOrDefault(InvoiceStatusEnum.PAID, 0L);
        long failedInvoices = statusMap.getOrDefault(InvoiceStatusEnum.FAILED, 0L);

        return SubscriptionDashboardResponse.builder()
                .totalRevenue(totalRevenue)
                .newSubscriptions(newSubscriptions)
                .renewalRate(renewalRate)
                .pendingInvoices(pendingInvoices)
                .paidInvoices(paidInvoices)
                .failedInvoices(failedInvoices)
                .dateFrom(dateFrom)
                .dateTo(dateTo)
                .build();
    }

    public SubscriptionDashboardChart getSubscriptionDashboardChart(LocalDateTime dateFrom, LocalDateTime dateTo) {
        boolean hasRange = dateFrom != null && dateTo != null;

        long teamSubscriptions = hasRange
                ? userSubscriptionRepository.countByPlanAndStartedAtBetween(PlanEnum.TEAM, dateFrom, dateTo)
                : userSubscriptionRepository.countByPlan(PlanEnum.TEAM);
        long businessSubscriptions = hasRange
                ? userSubscriptionRepository.countByPlanAndStartedAtBetween(PlanEnum.BUSINESS, dateFrom, dateTo)
                : userSubscriptionRepository.countByPlan(PlanEnum.BUSINESS);

        return SubscriptionDashboardChart.builder()
                .teamSubscriptions(teamSubscriptions)
                .businessSubscriptions(businessSubscriptions)
                .dateFrom(dateFrom)
                .dateTo(dateTo)
                .build();
    }


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
