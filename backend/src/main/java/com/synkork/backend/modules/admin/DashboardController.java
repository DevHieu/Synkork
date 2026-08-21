package com.synkork.backend.modules.admin;

import com.synkork.backend.common.response.ApiResponse;
import com.synkork.backend.modules.admin.report.AdminReportService;
import com.synkork.backend.modules.admin.rooms.AdminRoomService;
import com.synkork.backend.modules.admin.rooms.dtos.RoomDashboardStatsResponse;
import com.synkork.backend.modules.admin.rooms.dtos.RoomStatusCount;
import com.synkork.backend.modules.admin.statistics.StatisticsService;
import com.synkork.backend.modules.admin.statistics.dtos.*;
import com.synkork.backend.modules.admin.subscriptions.dtos.SubscriptionDashboardChart;
import com.synkork.backend.modules.admin.subscriptions.service.AdminSubscriptionService;
import com.synkork.backend.modules.admin.users.AdminUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/manage/dashboard")
public class DashboardController {

    @Autowired
    private StatisticsService statisticsService;

    @Autowired
    private AdminUserService adminUserService;

    @Autowired
    private AdminRoomService adminRoomService;

    @Autowired
    private AdminSubscriptionService adminSubscriptionService;

    @Autowired
    private AdminReportService adminReportService;

    @GetMapping("/overview/stats")
    private ResponseEntity<OverviewStatsResponse> getOverviewData(@ModelAttribute DateRangeRequest dateRange) {
        return ResponseEntity.ok(statisticsService.getOverviewStatsData(dateRange.dateFrom(), dateRange.dateTo()));
    }

    @GetMapping("/overview/chart")
    public ResponseEntity<List<OverviewChartResponse>> getOverviewChart(@ModelAttribute DateRangeRequest dateRange) {
        return ResponseEntity.ok(statisticsService.getOverviewChartData(dateRange.dateFrom(), dateRange.dateTo()));
    }

    @GetMapping("/users/stats")
    public ResponseEntity<UserStatsResponse> getUserStats(@ModelAttribute DateRangeRequest dateRange) {
        return ResponseEntity.ok(adminUserService.getUserStatsData(dateRange.dateFrom(), dateRange.dateTo()));
    }

    @GetMapping("/users/chart")
    public ResponseEntity<UserDashboardChartResponse> getUserChart(@ModelAttribute DateRangeRequest dateRange) {
        return ResponseEntity.ok(adminUserService.getUserChartData(dateRange.dateFrom(), dateRange.dateTo()));
    }

    @GetMapping("/rooms/stats")
    public ApiResponse<RoomDashboardStatsResponse> getStats(@ModelAttribute DateRangeRequest dateRange) {
        return ApiResponse.success(
                "Get room stats successfully",
                adminRoomService.getRoomStats(dateRange.dateFrom(), dateRange.dateTo())
        );
    }

    @GetMapping("/rooms/chart")
    public ApiResponse<List<RoomStatusCount>> getChart(@ModelAttribute DateRangeRequest dateRange) {
        return ApiResponse.success(
                "Get room chart successfully",
                adminRoomService.getRoomChart(dateRange.dateFrom(), dateRange.dateTo())
        );
    }

    @GetMapping("/subscriptions/stats")
    public ResponseEntity<SubscriptionDashboardResponse> getSubscriptionStats(@ModelAttribute DateRangeRequest dateRange) {
        return ResponseEntity.ok(adminSubscriptionService.getSubscriptionDashboardData(dateRange.dateFrom(), dateRange.dateTo()));
    }

    @GetMapping("/subscriptions/chart")
    public ResponseEntity<SubscriptionDashboardChart> getSubscriptionChart(@ModelAttribute DateRangeRequest dateRange) {
        return ResponseEntity.ok(adminSubscriptionService.getSubscriptionDashboardChart(dateRange.dateFrom(), dateRange.dateTo()));
    }

    @GetMapping("/reports/stats")
    public ResponseEntity<ReportStatsResponse> getReportStats(@ModelAttribute DateRangeRequest dateRange) {
        return ResponseEntity.ok(adminReportService.getReportStatsData(dateRange.dateFrom(), dateRange.dateTo()));
    }

    @GetMapping("/reports/chart")
    public ResponseEntity<List<ReportChartResponse>> getReportChart(
            @ModelAttribute DateRangeRequest dateRange
    ) {
        return ResponseEntity.ok(adminReportService.getReportChart(dateRange.dateFrom(), dateRange.dateTo()));
    }

    @GetMapping("/reports/top-reasons")
    public ResponseEntity<List<ReportReasonStatsResponse>> getReportReasonStats(@ModelAttribute DateRangeRequest dateRange) {
        return ResponseEntity.ok(adminReportService.getReportReasonStats(dateRange.dateFrom(), dateRange.dateTo()));
    }
}
