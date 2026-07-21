package com.synkork.backend.modules.admin.statistics;

import com.synkork.backend.modules.admin.statistics.dtos.*;
import com.synkork.backend.modules.admin.subscriptions.dtos.SubscriptionDashboardChart;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/manage")
public class StatisticsController {

    @Autowired
    private StatisticsService statisticsService;

    @GetMapping("/dashboard/overview/stats")
    private ResponseEntity<OverviewStatsResponse> getOverviewData(@ModelAttribute DateRangeRequest dateRange) {
        return ResponseEntity.ok(statisticsService.getOverviewStatsData(dateRange.dateFrom(), dateRange.dateTo()));
    }

    @GetMapping("/dashboard/overview/chart")
    public ResponseEntity<List<OverviewChartResponse>> getOverviewChart(@ModelAttribute DateRangeRequest dateRange) {
        return ResponseEntity.ok(statisticsService.getOverviewChartData(dateRange.dateFrom(), dateRange.dateTo()));
    }

    @GetMapping("/dashboard/users/stats")
    public ResponseEntity<UserStatsResponse> getUserStats(@ModelAttribute DateRangeRequest dateRange) {
        return ResponseEntity.ok(statisticsService.getUserStatsData(dateRange.dateFrom(), dateRange.dateTo()));
    }

    @GetMapping("/dashboard/users/chart")
    public ResponseEntity<UserDashboardChartResponse> getUserChart(@ModelAttribute DateRangeRequest dateRange) {
        return ResponseEntity.ok(statisticsService.getUserChartData(dateRange.dateFrom(), dateRange.dateTo()));
    }

    @GetMapping("/dashboard/subscriptions/stats")
    public ResponseEntity<SubscriptionDashboardResponse> getSubscriptionStats(@ModelAttribute DateRangeRequest dateRange) {
        return ResponseEntity.ok(statisticsService.getSubscriptionDashboardData(dateRange.dateFrom(), dateRange.dateTo()));
    }

    @GetMapping("/dashboard/subscriptions/chart")
    public ResponseEntity<SubscriptionDashboardChart> getSubscriptionChart(@ModelAttribute DateRangeRequest dateRange) {
        return ResponseEntity.ok(statisticsService.getSubscriptionDashboardChart(dateRange.dateFrom(), dateRange.dateTo()));
    }

    @GetMapping("/dashboard/reports/stats")
    public ResponseEntity<ReportStatsResponse> getReportStats(@ModelAttribute DateRangeRequest dateRange) {
        return ResponseEntity.ok(statisticsService.getReportStatsData(dateRange.dateFrom(), dateRange.dateTo()));
    }

    @GetMapping("/dashboard/reports/chart")
    public ResponseEntity<List<ReportChartResponse>> getReportChart(
            @ModelAttribute DateRangeRequest dateRange
    ) {
        return ResponseEntity.ok(statisticsService.getReportChart(dateRange.dateFrom(), dateRange.dateTo()));
    }

    @GetMapping("/dashboard/reports/top-reasons")
    public ResponseEntity<List<ReportReasonStatsResponse>> getReportReasonStats(@ModelAttribute DateRangeRequest dateRange) {
        return ResponseEntity.ok(statisticsService.getReportReasonStats(dateRange.dateFrom(), dateRange.dateTo()));
    }
}
