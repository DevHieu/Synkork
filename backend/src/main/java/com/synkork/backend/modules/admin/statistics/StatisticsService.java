package com.synkork.backend.modules.admin.statistics;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.synkork.backend.modules.admin.rooms.AdminRoomRepository;
import com.synkork.backend.modules.admin.rooms.AdminRoomService;
import com.synkork.backend.modules.admin.statistics.dtos.*;
import com.synkork.backend.modules.admin.users.AdminUserService;
import com.synkork.backend.modules.admin.utils.AdminUtils;
import com.synkork.backend.modules.payment.repository.UserSubscriptionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.synkork.backend.config.WebSocketEventListener;
import com.synkork.backend.modules.payment.enums.InvoiceStatusEnum;
import com.synkork.backend.modules.payment.repository.InvoiceRepository;
import com.synkork.backend.modules.room.enums.RoomTypeEnum;
import com.synkork.backend.modules.user.UserRepository;
import com.synkork.backend.modules.user.enums.RoleEnum;

@Service
public class StatisticsService {

    @Autowired
    private AdminUserService  adminUserService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AdminRoomService adminRoomService;

    @Autowired
    private AdminRoomRepository roomRepository;

    @Autowired
    private StatisticsRepository statisticsRepository;

    @Autowired
    private UserSubscriptionRepository userSubscriptionRepository;

    public void createStatistics() {
        LocalDateTime start = LocalDate.now().atStartOfDay();
        LocalDateTime end = start.plusDays(1);

        long newUsers = userRepository.countByRoleAndCreatedAtBetween(RoleEnum.USER, start, end);
        long newRooms = roomRepository.countByCreatedAtBetweenAndType(start, end, RoomTypeEnum.GROUP);
        long newSubscriptions = userSubscriptionRepository.countByCurrentTrueAndStartedAtBetween(start, end);
        long userOnlines = WebSocketEventListener.countedUsers.size();

        long totalUser = userRepository.countByRole(RoleEnum.USER);
        long totalRoom = roomRepository.countByType(RoomTypeEnum.GROUP);
        long totalSubscriptions = userSubscriptionRepository.countByCurrentTrue();

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


        WebSocketEventListener.countedUsers.clear();  // reset danh sách đã đếm để ngày mới đếm lại từ đầu

        statisticsRepository.save(statistics);
    }

    public List<OverviewChartResponse> getOverviewChartData(LocalDateTime dateFrom, LocalDateTime dateTo) {
        List<StatisticsEntity> statistics = dateFrom != null && dateTo != null
                ? statisticsRepository.findByCreatedAtBetweenOrderByCreatedAtAsc(dateFrom, dateTo)
                : statisticsRepository.findAll(Sort.by(Sort.Direction.ASC, "createdAt"));

        return statistics
                .stream()
                .map(s -> new OverviewChartResponse(
                        s.getCreatedAt().toLocalDate(),
                        s.getTotalUsers(),
                        s.getTotalRooms(),
                        s.getTotalSubscriptions()))
                .toList();
    }

    public OverviewStatsResponse getOverviewStatsData(LocalDateTime dateFrom, LocalDateTime dateTo) {
        boolean hasRange = dateFrom != null && dateTo != null;

        long totalUser = hasRange
                ? userRepository.countByRoleAndCreatedAtBetween(RoleEnum.USER, dateFrom, dateTo)
                : userRepository.countByRole(RoleEnum.USER);
        long totalRoom = hasRange
                ? roomRepository.countByTypeAndCreatedAtBetween(RoomTypeEnum.GROUP, dateFrom, dateTo)
                : roomRepository.countByType(RoomTypeEnum.GROUP);
        long totalSubscriptions = hasRange
                ? userSubscriptionRepository.countByCurrentTrueAndStartedAtBetween(dateFrom, dateTo)
                : userSubscriptionRepository.countByCurrentTrue();
        long userOnlines = WebSocketEventListener.onlineUsers.size();

        double userGrowth = 0;
        double roomGrowth = 0;
        double subscriptionGrowth = 0;
        double onlineGrowth = 0;

        if (hasRange) {
            userGrowth = adminUserService.calculateUserGrowth(dateFrom, dateTo, null);
            roomGrowth = adminRoomService.calculateRoomGrowth(dateFrom, dateTo, null);
            subscriptionGrowth =AdminUtils.calcGrowth(
                    userSubscriptionRepository.countByCurrentTrueAndStartedAtLessThanEqual(dateTo),
                    userSubscriptionRepository.countByCurrentTrueAndStartedAtLessThanEqual(dateFrom)
            );
        }

        return new OverviewStatsResponse(
                totalUser,
                userOnlines,
                totalRoom,
                totalSubscriptions,
                userGrowth,
                roomGrowth,
                subscriptionGrowth,
                onlineGrowth);
    }
}
