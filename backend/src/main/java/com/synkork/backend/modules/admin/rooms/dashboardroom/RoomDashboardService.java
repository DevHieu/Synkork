package com.synkork.backend.modules.admin.rooms.dashboardroom;

import com.synkork.backend.modules.room.enums.RoomTypeEnum;
import com.synkork.backend.modules.roomMember.RoomMemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class RoomDashboardService {

    @Autowired
    private DashboardRepository roomRepository;

    @Autowired
    private RoomMemberRepository roomMemberRepository;

    public RoomDashboardStatsResponse getStats(LocalDateTime dateFrom, LocalDateTime dateTo) {
        LocalDateTime effectiveTo = dateTo != null ? dateTo : LocalDateTime.now();
        LocalDateTime effectiveFrom = dateFrom != null ? dateFrom : effectiveTo.minusMonths(1);
        LocalDateTime previousFrom = dateFrom == null && dateTo == null
                ? effectiveFrom.minusMonths(1)
                : effectiveFrom.minus(Duration.between(effectiveFrom, effectiveTo));
        LocalDateTime previousTo = effectiveFrom;

        long totalRooms = roomRepository.count();
        long newRooms = roomRepository.countByCreatedAtBetween(effectiveFrom, effectiveTo);
        long previousRooms = roomRepository.countByCreatedAtBetween(previousFrom, previousTo);
        double roomGrowth = calculateGrowth(newRooms, previousRooms);

        long groupRooms = roomRepository.countByTypeAndCreatedAtBetween(RoomTypeEnum.GROUP, effectiveFrom, effectiveTo);
        long membersInGroupRooms = roomMemberRepository.countByRoomTypeAndRoomCreatedAtBetween(
                RoomTypeEnum.GROUP,
                effectiveFrom,
                effectiveTo
        );
        double averageMembersPerRoom = groupRooms == 0
                ? 0.0
                : Math.round(((double) membersInGroupRooms / groupRooms) * 10.0) / 10.0;

        long warnedRooms = roomRepository.countByWarningGreaterThanAndCreatedAtBetween(0, effectiveFrom, effectiveTo);

        return RoomDashboardStatsResponse.builder()
                .totalRooms(totalRooms)
                .newRooms(newRooms)
                .roomGrowth(roomGrowth)
                .averageMembersPerRoom(averageMembersPerRoom)
                .warnedRooms(warnedRooms)
                .build();
    }

    public List<RoomStatusCount> getChart(LocalDateTime dateFrom, LocalDateTime dateTo) {
        return roomRepository.countGroupByStatus(dateFrom, dateTo);
    }

    private double calculateGrowth(long current, long previous) {
        if (previous == 0) {
            return current > 0 ? 100.0 : 0.0;
        }

        double growth = ((double) (current - previous) / previous) * 100;
        return Math.round(growth * 10.0) / 10.0;
    }
}
