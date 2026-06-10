package com.synkork.backend.modules.report;

import com.synkork.backend.modules.report.dtos.ReportFilterRequest;
import com.synkork.backend.modules.report.enums.ReportStatusEnums;
import com.synkork.backend.modules.report.enums.ReportTypeEnums;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public final class ReportSpecification {

    private ReportSpecification() {}

    public static Specification<ReportEntity> from(ReportFilterRequest filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // --- search: reason ILIKE %keyword% OR description ILIKE %keyword%
            String search = filter.search();
            if (search != null && !search.isBlank()) {
                String pattern = "%" + search.trim().toLowerCase() + "%";
                Predicate byReason = cb.like(cb.lower(root.get("reason")), pattern);
                Predicate byDescription = cb.like(cb.lower(root.get("description")), pattern);
                predicates.add(cb.or(byReason, byDescription));
            }

            // --- status
            ReportStatusEnums status = filter.status();
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }

            // --- reportType
            ReportTypeEnums type = filter.reportType();
            if (type != null) {
                predicates.add(cb.equal(root.get("reportType"), type));
            }

            // --- fromDate (createdAt >= fromDate 00:00)
            LocalDateTime fromDate = filter.fromDate();
            if (fromDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(
                        root.get("createdAt"), fromDate));
            }

            // --- toDate (createdAt <= toDate 23:59:59)
            LocalDateTime toDate = filter.toDate();
            if (toDate != null) {
                predicates.add(cb.lessThanOrEqualTo(
                        root.get("createdAt"), toDate.plusDays(1)));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}