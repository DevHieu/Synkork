package com.synkork.backend.modules.report.dtos;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

import com.synkork.backend.modules.report.enums.ReportStatusEnums;
import com.synkork.backend.modules.report.enums.ReportTypeEnums;

public record ReportFilterRequest(
        String search,

        ReportStatusEnums status,

        ReportTypeEnums reportType,

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,

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