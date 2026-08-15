package com.synkork.backend.modules.admin.statistics;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class StatisticScheduler {

    @Autowired
    private StatisticsService statisticsService;

    @Transactional
    @Scheduled(cron = "0 0 0 * * *")
    public void createStatistics() {
        statisticsService.createStatistics();
    }
}
