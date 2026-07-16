package com.synkork.backend.modules.admin.rooms.dashboardroom;

import com.synkork.backend.common.response.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/manage/dashboard/rooms")
public class Roomdashboardcontroller {

    @Autowired
    private RoomDashboardService roomDashboardService;

    @GetMapping("/stats")
    public ApiResponse<RoomDashboardStatsResponse> getStats() {
        return ApiResponse.success("Get room stats successfully", roomDashboardService.getStats());
    }

    @GetMapping("/chart")
    public ApiResponse<List<RoomDashboardChartResponse>> getChart(
            @RequestParam(defaultValue = "WEEKLY") String period
    ) {
        return ApiResponse.success("Get room chart successfully", roomDashboardService.getChart(period));
    } 
}
