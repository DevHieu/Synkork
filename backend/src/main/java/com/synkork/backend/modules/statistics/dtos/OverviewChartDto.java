package com.synkork.backend.modules.statistics.dtos;

import java.time.LocalDate;

public record OverviewChartDto(LocalDate date, long totalUser, long totalRooms, long totalSubscriptions) {
}
