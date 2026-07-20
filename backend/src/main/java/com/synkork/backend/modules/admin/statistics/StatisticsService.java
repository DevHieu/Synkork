package com.synkork.backend.modules.admin.statistics;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.synkork.backend.modules.admin.statistics.dtos.*;
import com.synkork.backend.modules.admin.subscriptions.dtos.SubscriptionDashboardChart;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.synkork.backend.config.WebSocketEventListener;
import com.synkork.backend.modules.admin.statistics.enums.PeriodEnum;
import com.synkork.backend.modules.admin.subscriptions.dtos.AdminInvoiceResponse;
import com.synkork.backend.modules.message.MessageRepository;
import com.synkork.backend.modules.payment.enums.InvoiceStatusEnum;
import com.synkork.backend.modules.payment.repository.InvoiceRepository;
import com.synkork.backend.modules.payment.repository.UserSubscriptionRepository;
import com.synkork.backend.modules.report.ReportRepository;
import com.synkork.backend.modules.report.enums.ReportStatusEnums;
import com.synkork.backend.modules.report.enums.ReportTypeEnums;
import com.synkork.backend.modules.room.RoomRepository;
import com.synkork.backend.modules.room.enums.RoomTypeEnum;
import com.synkork.backend.modules.user.UserRepository;
import com.synkork.backend.modules.user.enums.PlanEnum;
import com.synkork.backend.modules.user.enums.RoleEnum;
import com.synkork.backend.modules.user.enums.UserStatusEnum;

@Service
public class StatisticsService {

    private static final List<PlanEnum> PAID_PLANS = List.of(PlanEnum.TEAM, PlanEnum.BUSINESS);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private StatisticsRepository statisticsRepository;

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private UserSubscriptionRepository userSubscriptionRepository;

    @Autowired
    private ReportRepository reportRepository;

    private LocalDateTime getStart(PeriodEnum period) {

        return switch (period) {
            case WEEKLY -> LocalDateTime.now().minusDays(7);
            case MONTHLY -> LocalDateTime.now().minusMonths(1);
            case QUARTERLY -> LocalDateTime.now().minusMonths(3);
            case YEARLY -> LocalDateTime.now().minusYears(1);
        };
    }

    private double calcGrowth(long current, long previous) {
        System.out.println("current: " + current + " previous: " + previous);
        if (previous == 0)
            return 100.0;
        return Math.round(((double) (current - previous) / previous) * 1000.0) / 10.0;
    }

    private double calcRate(long current, long total) {
        if (total == 0)
            return 0.0;
        return Math.round(((double) current / total) * 1000.0) / 10.0;
    }

    public void createStatistics() {
        LocalDateTime start = LocalDate.now().atStartOfDay();
        LocalDateTime end = start.plusDays(1);

        long newUsers = userRepository.countByCreatedAtBetweenAndRole(start, end, RoleEnum.USER);
        long newRooms = roomRepository.countByCreatedAtBetweenAndType(start, end, RoomTypeEnum.GROUP);
        long newSubscriptions = invoiceRepository.countByStatusAndPaidAtBetween(InvoiceStatusEnum.PAID, start, end);
        long userOnlines = WebSocketEventListener.onlineUserCounter;

        long totalUser = userRepository.countByRole(RoleEnum.USER);
        long totalRoom = roomRepository.countByType(RoomTypeEnum.GROUP);
        long totalSubscriptions = invoiceRepository.countByStatus(InvoiceStatusEnum.PAID);

        StatisticsEntity statistics = StatisticsEntity.builder()
                .createdAt(start)
                .newUsers(newUsers)
                .newRooms(newRooms)
                .newSubscriptions(newSubscriptions)
                .userOnlines(userOnlines)
                .totalUsers(totalUser)
                .totalRooms(totalRoom)
                .totalSubscriptions(totalSubscriptions)
                .build();

        WebSocketEventListener.onlineUserCounter = 0; // reset về lại 0

        statisticsRepository.save(statistics);
    }

    public List<OverviewChartResponse> getOverviewChartData(PeriodEnum period) {

        LocalDateTime from = getStart(period);

        return statisticsRepository.findByDateRange(from)
                .stream()
                .map(s -> new OverviewChartResponse(
                        s.getCreatedAt().toLocalDate(),
                        s.getTotalUsers(),
                        s.getTotalRooms(),
                        s.getTotalSubscriptions()))
                .toList();
    }

    public OverviewStatsResponse getOverviewStatsData() {

        long totalUser = userRepository.countByRole(RoleEnum.USER);
        long totalRoom = roomRepository.countByType(RoomTypeEnum.GROUP);
        long totalSubscriptions = invoiceRepository.countByStatus(InvoiceStatusEnum.PAID);
        long userOnlines = WebSocketEventListener.onlineUsers.size();

        StatisticsEntity yesterdayStats = statisticsRepository
                .findByDate(LocalDate.now().minusDays(1).atStartOfDay())
                .orElse(null);

        StatisticsEntity monthBeforeStats = statisticsRepository
                .findByDate(LocalDate.now().minusMonths(1).atStartOfDay())
                .orElse(null);

        double userDayGrowth = 0;
        double roomDayGrowth = 0;
        double subscriptionDayGrowth = 0;
        double onlineDayGrowth = 0;

        double userMonthGrowth = 0;
        double roomMonthGrowth = 0;
        double subscriptionMonthGrowth = 0;

        if (yesterdayStats != null) {
            userDayGrowth = calcGrowth(totalUser, yesterdayStats.getTotalUsers());
            roomDayGrowth = calcGrowth(totalRoom, yesterdayStats.getTotalRooms());
            subscriptionDayGrowth = calcGrowth(totalSubscriptions, yesterdayStats.getTotalSubscriptions());
            onlineDayGrowth = calcGrowth(userOnlines, yesterdayStats.getUserOnlines());
        }

        if (monthBeforeStats != null) {
            userMonthGrowth = calcGrowth(totalUser, monthBeforeStats.getTotalUsers());
            roomMonthGrowth = calcGrowth(totalRoom, monthBeforeStats.getTotalRooms());
            subscriptionMonthGrowth = calcGrowth(totalSubscriptions, monthBeforeStats.getTotalSubscriptions());
        }

        return new OverviewStatsResponse(
                totalUser,
                userOnlines,
                totalRoom,
                totalSubscriptions,
                userDayGrowth,
                roomDayGrowth,
                subscriptionDayGrowth,
                onlineDayGrowth,
                userMonthGrowth,
                roomMonthGrowth,
                subscriptionMonthGrowth);
    }

    public UserStatsResponse getUserStatsData() {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime startOfTomorrow = today.plusDays(1).atStartOfDay();
        LocalDateTime startOfMonth = today.withDayOfMonth(1).atStartOfDay();

        RoleEnum userRole = RoleEnum.USER;

        return new UserStatsResponse(
                userRepository.countByRole(userRole),
                userRepository.countByCreatedAtBetweenAndRole(startOfDay, startOfTomorrow, userRole),
                userRepository.countByCreatedAtBetweenAndRole(startOfMonth, startOfTomorrow, userRole),
                userRepository.countByRoleAndStatus(userRole, UserStatusEnum.ACTIVE),
                userRepository.countByRoleAndStatus(userRole, UserStatusEnum.INACTIVE),
                userRepository.countByRoleAndStatus(userRole, UserStatusEnum.BANNED),
                userRepository.countByRoleAndCurrentPlan(userRole, PlanEnum.FREE),
                userRepository.countByRoleAndCurrentPlan(userRole, PlanEnum.TEAM),
                userRepository.countByRoleAndCurrentPlan(userRole, PlanEnum.BUSINESS));
    }

    public SubscriptionDashboardResponse getSubscriptionDashboardData(LocalDateTime dateFrom, LocalDateTime dateTo) {
        boolean hasRange = dateFrom != null && dateTo != null;

        BigDecimal totalRevenue = invoiceRepository.sumAmountByStatus(InvoiceStatusEnum.PAID, dateFrom, dateTo);

        long newSubscriptions = userSubscriptionRepository.countByPlanIn(PAID_PLANS, dateFrom, dateTo);
        long renewedSubscriptions = userSubscriptionRepository.countRenewedPaidSubscriptions(PAID_PLANS, dateFrom, dateTo);
        double renewalRate = calcRate(renewedSubscriptions, newSubscriptions);

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

    public ReportStatsResponse getReportStatsData() {
        long total = reportRepository.count();
        long pending = reportRepository.countByStatus(ReportStatusEnums.PENDING);
        long resolved = reportRepository.countByStatus(ReportStatusEnums.RESOLVED);
        long dismissed = reportRepository.countByStatus(ReportStatusEnums.DISMISSED);
        long userReports = reportRepository.countByReportType(ReportTypeEnums.USER);
        long roomReports = reportRepository.countByReportType(ReportTypeEnums.ROOM);

        return new ReportStatsResponse(total, pending, resolved, dismissed, userReports, roomReports);
    }

    public List<ReportChartResponse> getReportChart(PeriodEnum period) {
        LocalDateTime from = getStart(period);

        return reportRepository.findDailyReportCounts(from)
                .stream()
                .map(row -> new ReportChartResponse(
                        (LocalDate) row[0],
                        ((Number) row[1]).longValue(),
                        ((Number) row[2]).longValue()))
                .toList();
    }

    public List<ReportReasonStatsResponse> getReportReasonStats() {
        return reportRepository.findReasonCountsGroupedByType();
    }
}
