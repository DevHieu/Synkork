package com.synkork.backend.modules.admin.workspace.rooms.dashboardroom;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RoomDashboardStatsResponse {
    private long totalRooms;
    private long openRooms;
    private long lockedRooms;
    private long groupRooms;
    private long dmRooms;
    private double dayGrowth;
    private double monthGrowth;
}
