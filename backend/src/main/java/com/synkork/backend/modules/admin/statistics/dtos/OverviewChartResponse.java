package com.synkork.backend.modules.admin.statistics.dtos;

import java.time.LocalDate;

public record OverviewChartResponse(LocalDate date, long totalUser, long totalRooms, long totalSubscriptions) {
}
