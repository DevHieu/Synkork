package com.synkork.backend.modules.report;

import com.synkork.backend.modules.report.dtos.ReportFilterRequest;
import com.synkork.backend.modules.report.enums.ReportStatusEnums;
import com.synkork.backend.modules.report.enums.ReportTypeEnums;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
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

            // --- dateFrom (createdAt >= dateFrom 00:00)
            LocalDate dateFrom = filter.dateFrom();
            if (dateFrom != null) {
                predicates.add(cb.greaterThanOrEqualTo(
                        root.get("createdAt"), dateFrom.atStartOfDay()));
            }

            // --- dateTo (createdAt <= dateTo 23:59:59)
            LocalDate dateTo = filter.dateTo();
            if (dateTo != null) {
                predicates.add(cb.lessThanOrEqualTo(
                        root.get("createdAt"), dateTo.plusDays(1).atStartOfDay()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}