package com.synkork.backend.modules.admin.statistics;

import com.synkork.backend.modules.admin.statistics.dtos.*;
import com.synkork.backend.modules.admin.statistics.enums.PeriodEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/manage")
public class StatisticsController {

    @Autowired
    private StatisticsService statisticsService;

    @GetMapping("/dashboard/overview/stats")
    private ResponseEntity<OverviewStatsResponse> getOverviewData() {
        return ResponseEntity.ok(statisticsService.getOverviewStatsData());
    }

    @GetMapping("/dashboard/overview/chart")
    public ResponseEntity<List<OverviewChartResponse>> getOverviewChart(@RequestParam String period) {
        PeriodEnum periodEnum = PeriodEnum.valueOf(period);

        return ResponseEntity.ok(statisticsService.getOverviewChartData(periodEnum));
    }

    @GetMapping("/dashboard/users/stats")
    public ResponseEntity<UserStatsResponse> getUserStats() {
        return ResponseEntity.ok(statisticsService.getUserStatsData());
    }

    @GetMapping("/dashboard/subscriptions/stats")
    public ResponseEntity<SubscriptionDashboardResponse> getSubscriptionStats() {
        return ResponseEntity.ok(statisticsService.getSubscriptionDashboardData());
    }

    @GetMapping("/dashboard/reports/stats")
    public ResponseEntity<ReportStatsResponse> getReportStats() {
        return ResponseEntity.ok(statisticsService.getReportStatsData());
    }

    @GetMapping("/dashboard/reports/chart")
    public ResponseEntity<List<ReportChartResponse>> getReportChart(
            @RequestParam(defaultValue = "MONTHLY") String period
    ) {
        PeriodEnum periodEnum = PeriodEnum.valueOf(period);
        return ResponseEntity.ok(statisticsService.getReportChart(periodEnum));
    }

    @GetMapping("/dashboard/reports/top-reasons")
    public ResponseEntity<List<ReportReasonStatsResponse>> getReportReasonStats() {
        return ResponseEntity.ok(statisticsService.getReportReasonStats());
    }
}

