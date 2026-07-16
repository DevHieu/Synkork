package com.synkork.backend.modules.admin.rooms.dashboardroom;

import com.synkork.backend.modules.room.enums.RoomStatusEnum;
import com.synkork.backend.modules.room.enums.RoomTypeEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class RoomDashboardService {

    @Autowired
    private DashboardRepository roomRepository;

    public RoomDashboardStatsResponse getStats() {
        long totalRooms = roomRepository.count();
        long openRooms = roomRepository.countByStatus(RoomStatusEnum.OPEN);
        long lockedRooms = roomRepository.countByStatus(RoomStatusEnum.LOCKED);
        long groupRooms = roomRepository.countByType(RoomTypeEnum.GROUP);
        long dmRooms = roomRepository.countByType(RoomTypeEnum.DM);

        // Day growth: so sánh hôm nay với hôm qua
         LocalDateTime startOfToday = LocalDate.now().atStartOfDay();
        LocalDateTime startOfYesterday = startOfToday.minusDays(1);

        long todayCount = roomRepository.countByCreatedAtBetween(startOfToday, LocalDateTime.now());
        long yesterdayCount = roomRepository.countByCreatedAtBetween(startOfYesterday, startOfToday);
        double dayGrowth = yesterdayCount == 0
                ? (todayCount > 0 ? 100.0 : 0.0)
                : ((double)(todayCount - yesterdayCount) / yesterdayCount) * 100;

        // Month growth: so sánh tháng này với tháng trước
        LocalDateTime startOfThisMonth = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        LocalDateTime startOfLastMonth = startOfThisMonth.minusMonths(1);

        long thisMonthCount = roomRepository.countByCreatedAtBetween(startOfThisMonth, LocalDateTime.now());
        long lastMonthCount = roomRepository.countByCreatedAtBetween(startOfLastMonth, startOfThisMonth);
        double monthGrowth = lastMonthCount == 0
                ? (thisMonthCount > 0 ? 100.0 : 0.0)
                : ((double)(thisMonthCount - lastMonthCount) / lastMonthCount) * 100;

        return RoomDashboardStatsResponse.builder()
                .totalRooms(totalRooms)
                .openRooms(openRooms)
                .lockedRooms(lockedRooms)
                .groupRooms(groupRooms)
                .dmRooms(dmRooms)
                .dayGrowth(Math.round(dayGrowth * 10.0) / 10.0)
                .monthGrowth(Math.round(monthGrowth * 10.0) / 10.0)
                .build();
    }

    public List<RoomDashboardChartResponse> getChart(String period) {
        List<RoomDashboardChartResponse> result = new ArrayList<>();
        LocalDate today = LocalDate.now();

        List<LocalDate> dates = switch (period) {
            case "MONTHLY" -> getLast12Months(today);
            case "QUARTERLY" -> getLast4Quarters(today);
            case "YEARLY" -> getLast5Years(today);
            default -> getLast7Weeks(today); // WEEKLY
        };

        for (int i = 0; i < dates.size() - 1; i++) {
            LocalDate from = dates.get(i);
            LocalDate to = dates.get(i + 1);

            LocalDateTime fromDt = from.atStartOfDay();
            LocalDateTime toDt = to.atStartOfDay();

            long total = roomRepository.countByCreatedAtBefore(toDt);
            long open = roomRepository.countByStatusAndCreatedAtBefore(RoomStatusEnum.OPEN, toDt);
            long locked = roomRepository.countByStatusAndCreatedAtBefore(RoomStatusEnum.LOCKED, toDt);

            result.add(new RoomDashboardChartResponse(from, total, open, locked));
        }

        return result;
    }

    // --- Helpers tạo danh sách mốc thời gian ---

    private List<LocalDate> getLast7Weeks(LocalDate today) {
        List<LocalDate> dates = new ArrayList<>();
        LocalDate monday = today.with(java.time.DayOfWeek.MONDAY);
        for (int i = 7; i >= 0; i--) {
            dates.add(monday.minusWeeks(i));
        }
        return dates;
    }

    private List<LocalDate> getLast12Months(LocalDate today) {
        List<LocalDate> dates = new ArrayList<>();
        LocalDate firstOfMonth = today.withDayOfMonth(1);
        for (int i = 12; i >= 0; i--) {
            dates.add(firstOfMonth.minusMonths(i));
        }
        return dates;
    }

    private List<LocalDate> getLast4Quarters(LocalDate today) {
        List<LocalDate> dates = new ArrayList<>();
        LocalDate startOfQuarter = today.with(today.getMonth().firstMonthOfQuarter())
                .withDayOfMonth(1);
        for (int i = 4; i >= 0; i--) {
            dates.add(startOfQuarter.minusMonths((long) i * 3));
        }
        return dates;
    }

    private List<LocalDate> getLast5Years(LocalDate today) {
        List<LocalDate> dates = new ArrayList<>();
        LocalDate firstOfYear = today.withDayOfYear(1);
        for (int i = 5; i >= 0; i--) {
            dates.add(firstOfYear.minusYears(i));
        }
        return dates;
    }
}
