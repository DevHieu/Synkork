package com.synkork.backend.modules.admin.statistics.dtos;

import java.time.LocalDate;

public record UserChartResponse(
        LocalDate date,
        long newUsers
) {
}
