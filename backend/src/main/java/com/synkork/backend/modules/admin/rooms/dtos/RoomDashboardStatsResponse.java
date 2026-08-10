package com.synkork.backend.modules.admin.rooms.dtos;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RoomDashboardStatsResponse {
    private long totalRooms;
    private long newRooms;
    private double roomGrowth;
    private double averageMembersPerRoom;
    private long warnedRooms;
}
 
