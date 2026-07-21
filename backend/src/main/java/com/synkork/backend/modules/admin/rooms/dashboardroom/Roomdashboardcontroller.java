package com.synkork.backend.modules.admin.rooms.dashboardroom;

import com.synkork.backend.common.response.ApiResponse;
import com.synkork.backend.modules.admin.statistics.dtos.DateRangeRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/manage/dashboard/rooms")
public class Roomdashboardcontroller {

    @Autowired
    private RoomDashboardService roomDashboardService;

    @GetMapping("/stats")
    public ApiResponse<RoomDashboardStatsResponse> getStats(@ModelAttribute DateRangeRequest dateRange) {
        return ApiResponse.success(
                "Get room stats successfully",
                roomDashboardService.getStats(dateRange.dateFrom(), dateRange.dateTo())
        );
    }

    @GetMapping("/chart")
    public ApiResponse<List<RoomStatusCount>> getChart(@ModelAttribute DateRangeRequest dateRange) {
        return ApiResponse.success(
                "Get room chart successfully",
                roomDashboardService.getChart(dateRange.dateFrom(), dateRange.dateTo())
        );
    } 
}
