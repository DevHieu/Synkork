package com.synkork.backend.modules.admin.rooms.dashboardroom;

import com.synkork.backend.modules.room.enums.RoomStatusEnum;
import com.synkork.backend.modules.room.enums.RoomTypeEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class RoomDashboardService {

    @Autowired
    private DashboardRepository roomRepository;

    public RoomDashboardStatsResponse getStats(LocalDateTime dateFrom, LocalDateTime dateTo) {
        boolean hasRange = dateFrom != null && dateTo != null;

        long totalRooms = hasRange ? roomRepository.countByCreatedAtBetween(dateFrom, dateTo) : roomRepository.count();
        long openRooms = hasRange
                ? roomRepository.countByStatusAndCreatedAtBetween(RoomStatusEnum.OPEN, dateFrom, dateTo)
                : roomRepository.countByStatus(RoomStatusEnum.OPEN);
        long lockedRooms = hasRange
                ? roomRepository.countByStatusAndCreatedAtBetween(RoomStatusEnum.LOCKED, dateFrom, dateTo)
                : roomRepository.countByStatus(RoomStatusEnum.LOCKED);
        long groupRooms = hasRange
                ? roomRepository.countByTypeAndCreatedAtBetween(RoomTypeEnum.GROUP, dateFrom, dateTo)
                : roomRepository.countByType(RoomTypeEnum.GROUP);
        long dmRooms = hasRange
                ? roomRepository.countByTypeAndCreatedAtBetween(RoomTypeEnum.DM, dateFrom, dateTo)
                : roomRepository.countByType(RoomTypeEnum.DM);

        double dayGrowth;
        double monthGrowth;

        if (hasRange) {
            Duration periodLength = Duration.between(dateFrom, dateTo);
            LocalDateTime previousFrom = dateFrom.minus(periodLength);
            LocalDateTime previousTo = dateFrom;
            dayGrowth = calculateGrowth(totalRooms, roomRepository.countByCreatedAtBetween(previousFrom, previousTo));
            monthGrowth = dayGrowth;
        }
        else {
            LocalDateTime startOfToday = LocalDate.now().atStartOfDay();
            LocalDateTime startOfYesterday = startOfToday.minusDays(1);

            long todayCount = roomRepository.countByCreatedAtBetween(startOfToday, LocalDateTime.now());
            long yesterdayCount = roomRepository.countByCreatedAtBetween(startOfYesterday, startOfToday);
            dayGrowth = calculateGrowth(todayCount, yesterdayCount);

            LocalDateTime startOfThisMonth = LocalDate.now().withDayOfMonth(1).atStartOfDay();
            LocalDateTime startOfLastMonth = startOfThisMonth.minusMonths(1);

            long thisMonthCount = roomRepository.countByCreatedAtBetween(startOfThisMonth, LocalDateTime.now());
            long lastMonthCount = roomRepository.countByCreatedAtBetween(startOfLastMonth, startOfThisMonth);
            monthGrowth = calculateGrowth(thisMonthCount, lastMonthCount);
        }

        return RoomDashboardStatsResponse.builder()
                .totalRooms(totalRooms)
                .openRooms(openRooms)
                .lockedRooms(lockedRooms)
                .groupRooms(groupRooms)
                .dmRooms(dmRooms)
                .dayGrowth(dayGrowth)
                .monthGrowth(monthGrowth)
                .build();
    }

    public List<RoomDashboardChartResponse> getChart(String period, LocalDateTime dateFrom, LocalDateTime dateTo) {
        if (dateFrom != null && dateTo != null) {
            return getRangeChart(period, dateFrom, dateTo);
        }

        List<RoomDashboardChartResponse> result = new ArrayList<>();
        LocalDate today = LocalDate.now();

        List<LocalDate> dates = switch (period) {
            case "MONTHLY" -> getLast12Months(today);
            case "QUARTERLY" -> getLast4Quarters(today);
            case "YEARLY" -> getLast5Years(today);
            default -> getLast7Weeks(today);
        };

        for (int i = 0; i < dates.size() - 1; i++) {
            LocalDate from = dates.get(i);
            LocalDate to = dates.get(i + 1);

            LocalDateTime toDt = to.atStartOfDay();

            long total = roomRepository.countByCreatedAtBefore(toDt);
            long open = roomRepository.countByStatusAndCreatedAtBefore(RoomStatusEnum.OPEN, toDt);
            long locked = roomRepository.countByStatusAndCreatedAtBefore(RoomStatusEnum.LOCKED, toDt);

            result.add(new RoomDashboardChartResponse(from, total, open, locked));
        }

        return result;
    }

    private List<RoomDashboardChartResponse> getRangeChart(String period, LocalDateTime dateFrom, LocalDateTime dateTo) {
        List<RoomDashboardChartResponse> result = new ArrayList<>();
        LocalDateTime cursor = dateFrom;

        while (cursor.isBefore(dateTo)) {
            LocalDateTime next = nextBucket(cursor, period);
            if (next.isAfter(dateTo)) {
                next = dateTo;
            }

            long total = roomRepository.countByCreatedAtBetween(cursor, next);
            long open = roomRepository.countByStatusAndCreatedAtBetween(RoomStatusEnum.OPEN, cursor, next);
            long locked = roomRepository.countByStatusAndCreatedAtBetween(RoomStatusEnum.LOCKED, cursor, next);

            result.add(new RoomDashboardChartResponse(cursor.toLocalDate(), total, open, locked));
            cursor = next;
        }

        return result;
    }

    private LocalDateTime nextBucket(LocalDateTime from, String period) {
        return switch (period) {
            case "MONTHLY" -> from.plusMonths(1);
            case "QUARTERLY" -> from.plusMonths(3);
            case "YEARLY" -> from.plusYears(1);
            default -> from.plusWeeks(1);
        };
    }

    private double calculateGrowth(long current, long previous) {
        if (previous == 0) {
            return current > 0 ? 100.0 : 0.0;
        }

        double growth = ((double) (current - previous) / previous) * 100;
        return Math.round(growth * 10.0) / 10.0;
    }

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
