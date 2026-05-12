package com.synkork.backend.modules.statistics;

import com.synkork.backend.config.WebSocketEventListener;
import com.synkork.backend.modules.message.MessageRepository;
import com.synkork.backend.modules.room.RoomRepository;
import com.synkork.backend.modules.room.enums.RoomTypeEnum;
import com.synkork.backend.modules.statistics.dtos.OverviewChartDto;
import com.synkork.backend.modules.statistics.dtos.OverviewStatsData;
import com.synkork.backend.modules.statistics.enums.PeriodEnum;
import com.synkork.backend.modules.subscription.UserSubscriptionRepository;
import com.synkork.backend.modules.user.UserRepository;
import com.synkork.backend.modules.user.enums.RoleEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
    private UserSubscriptionRepository userSubscriptionRepository;

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
        long newSubscriptions = userSubscriptionRepository.countByCreatedAtBetween(start, end);
        long userOnlines = WebSocketEventListener.onlineUserCounter;

        long totalUser = userRepository.countByRole(RoleEnum.USER);
        long totalRoom = roomRepository.countByType(RoomTypeEnum.GROUP);
        long totalSubscriptions = userSubscriptionRepository.count();

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

    public List<OverviewChartDto> getOverviewChartData(PeriodEnum period) {

        LocalDateTime from = getStart(period);

        return statisticsRepository.findByDateRange(from)
                .stream()
                .map(s -> new OverviewChartDto(
                        s.getCreatedAt().toLocalDate(),
                        s.getTotalUsers(),
                        s.getTotalRooms(),
                        s.getTotalSubscriptions()
                ))
                .toList();
    }

    public OverviewStatsData getOverviewStatsData() {

        long totalUser = userRepository.countByRole(RoleEnum.USER);
        long totalRoom = roomRepository.countByType(RoomTypeEnum.GROUP);
        long totalSubscriptions = userSubscriptionRepository.count();
        long userOnlines = WebSocketEventListener.onlineUsers.size();

        StatisticsEntity yesterdayStats = statisticsRepository.findByDate(LocalDate.now().minusDays(1).atStartOfDay()).orElse(null);
        StatisticsEntity monthBeforeStats = statisticsRepository.findByDate(LocalDate.now().minusMonths(1).atStartOfDay()).orElse(null);

        // % so với hôm qua
        assert yesterdayStats != null;
        double userDayGrowth = calcGrowth(totalUser, yesterdayStats.getTotalUsers());
        double roomDayGrowth = calcGrowth(totalRoom, yesterdayStats.getTotalRooms());
        double subscriptionDayGrowth = calcGrowth(totalSubscriptions, yesterdayStats.getTotalSubscriptions());
        double onlineDayGrowth = calcGrowth(totalUser, yesterdayStats.getUserOnlines());

        // % so với tháng trước
        assert monthBeforeStats != null;
        double userMonthGrowth = calcGrowth(totalUser, monthBeforeStats.getTotalUsers());
        double roomMonthGrowth = calcGrowth(totalRoom, monthBeforeStats.getTotalRooms());
        double subscriptionMonthGrowth = calcGrowth(totalSubscriptions, monthBeforeStats.getTotalSubscriptions());

        return new OverviewStatsData(
                totalUser,
                userOnlines,
                totalRoom,
                totalSubscriptions,
                userDayGrowth, roomDayGrowth, subscriptionDayGrowth, onlineDayGrowth,
                userMonthGrowth, roomMonthGrowth, subscriptionMonthGrowth
        );
    }
}
