package com.synkork.backend.modules.statistics;

import com.synkork.backend.modules.statistics.dtos.OverviewChartDto;
import com.synkork.backend.modules.statistics.dtos.OverviewStatsData;
import com.synkork.backend.modules.statistics.enums.PeriodEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/public")
public class StatisticsController {

    @Autowired
    private StatisticsService statisticsService;

    @GetMapping("/dashboard/overview/stats")
    private ResponseEntity<OverviewStatsData> getOverviewData() {
        return ResponseEntity.ok(statisticsService.getOverviewStatsData());
    }

    @GetMapping("/dashboard/overview/chart")
    public ResponseEntity<List<OverviewChartDto>> getOverviewChart(@RequestParam String period) {
        PeriodEnum periodEnum = PeriodEnum.valueOf(period);

        return ResponseEntity.ok(statisticsService.getOverviewChartData(periodEnum));
    }
}
