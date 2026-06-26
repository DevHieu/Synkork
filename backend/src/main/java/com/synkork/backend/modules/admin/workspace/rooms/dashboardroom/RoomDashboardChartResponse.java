package com.synkork.backend.modules.admin.workspace.rooms.dashboardroom;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class RoomDashboardChartResponse {
    private LocalDate date;
    private long totalRooms;
    private long openRooms;
    private long lockedRooms;
}
