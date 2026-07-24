package com.synkork.backend.modules.admin.statistics.dtos;

import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

public record DateRangeRequest(
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        LocalDateTime dateFrom,

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        LocalDateTime dateTo
) {}