package com.synkork.backend.modules.report.dtos;

import java.time.LocalDateTime;

import org.springframework.format.annotation.DateTimeFormat;

import com.synkork.backend.modules.report.enums.ReportStatusEnums;
import com.synkork.backend.modules.report.enums.ReportTypeEnums;

public record ReportFilterRequest(
        String search,
        ReportStatusEnums status,
        ReportTypeEnums reportType,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate,
        Integer page,
        Integer size
) {
    public int getPage() {
        return page != null ? page : 0;
    }

    public int getSize() {
        return size != null ? size : 10;
    }
}