package com.synkork.backend.modules.admin.statistics;

import com.synkork.backend.config.WebSocketEventListener;
import com.synkork.backend.modules.admin.statistics.dtos.UserStatsResponse;
import com.synkork.backend.modules.message.MessageRepository;
import com.synkork.backend.modules.payment.InvoiceRepository;
import com.synkork.backend.modules.payment.enums.InvoiceStatusEnum;
import com.synkork.backend.modules.report.ReportRepository;
import com.synkork.backend.modules.report.enums.ReportStatusEnums;
import com.synkork.backend.modules.report.enums.ReportTypeEnums;
import com.synkork.backend.modules.room.RoomRepository;
import com.synkork.backend.modules.room.enums.RoomTypeEnum;
import com.synkork.backend.modules.admin.statistics.dtos.OverviewChartResponse;
import com.synkork.backend.modules.admin.statistics.dtos.OverviewStatsResponse;
import com.synkork.backend.modules.admin.statistics.dtos.ReportChartResponse;
import com.synkork.backend.modules.admin.statistics.dtos.ReportStatsResponse;
import com.synkork.backend.modules.admin.statistics.dtos.SubscriptionDashboardResponse;
import com.synkork.backend.modules.admin.statistics.enums.PeriodEnum;
import com.synkork.backend.modules.user.UserRepository;
import com.synkork.backend.modules.user.enums.PlanEnum;
import com.synkork.backend.modules.user.enums.RoleEnum;
import com.synkork.backend.modules.admin.subscriptions.dtos.AdminInvoiceResponse;
import com.synkork.backend.modules.user.enums.UserStatusEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class StatisticsService {

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
        if (previous == 0) return 100.0;
        return Math.round(((double) (current - previous) / previous) * 1000.0) / 10.0;
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
                        s.getTotalSubscriptions()
                ))
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
                subscriptionMonthGrowth
        );
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
                userRepository.countByRoleAndCurrentPlan(userRole, PlanEnum.BUSINESS)
        );
    }

    public SubscriptionDashboardResponse getSubscriptionDashboardData() {
        LocalDateTime startOfMonth = LocalDate.now().withDayOfMonth(1).atStartOfDay();

        BigDecimal totalRevenue = invoiceRepository.sumAmountByStatus(InvoiceStatusEnum.PAID);
        BigDecimal revenueThisMonth = invoiceRepository.sumAmountByStatusAndPaidAtAfter(InvoiceStatusEnum.PAID, startOfMonth);

        long activeSubscriptions = userRepository.countActiveSubscriptions(PlanEnum.FREE, LocalDateTime.now());
        long pendingInvoices = invoiceRepository.countByStatus(InvoiceStatusEnum.PENDING);
        long paidInvoices = invoiceRepository.countByStatus(InvoiceStatusEnum.PAID);
        long failedInvoices = invoiceRepository.countByStatus(InvoiceStatusEnum.FAILED);

        Pageable pageable = PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "createdAt"));
        List<AdminInvoiceResponse> recentTransactions = invoiceRepository.findAll(pageable)
                .stream()
                .map(AdminInvoiceResponse::from)
                .toList();

        return SubscriptionDashboardResponse.builder()
                .totalRevenue(totalRevenue)
                .revenueThisMonth(revenueThisMonth)
                .activeSubscriptions(activeSubscriptions)
                .pendingInvoices(pendingInvoices)
                .paidInvoices(paidInvoices)
                .failedInvoices(failedInvoices)
                .recentTransactions(recentTransactions)
                .build();
    }

    public ReportStatsResponse getReportStatsData() {
        long total      = reportRepository.count();
        long pending    = reportRepository.countByStatus(ReportStatusEnums.PENDING);
        long resolved   = reportRepository.countByStatus(ReportStatusEnums.RESOLVED);
        long dismissed   = reportRepository.countByStatus(ReportStatusEnums.DISMISSED);
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
                        ((Number) row[2]).longValue()
                ))
                .toList();
    }
}

