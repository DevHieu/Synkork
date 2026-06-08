package com.synkork.backend.modules.report.dtos;

import java.util.List;

public record ReportPageResponse(
        List<ReportDTO> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
}