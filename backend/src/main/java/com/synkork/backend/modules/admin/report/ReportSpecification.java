package com.synkork.backend.modules.admin.report;

import com.synkork.backend.modules.admin.report.dtos.ReportFilterRequest;
import com.synkork.backend.modules.report.ReportEntity;
import com.synkork.backend.modules.report.enums.ReportSeverityEnums;
import com.synkork.backend.modules.report.enums.ReportStatusEnums;
import com.synkork.backend.modules.report.enums.ReportTypeEnums;
import com.synkork.backend.modules.room.RoomEntity;
import com.synkork.backend.modules.user.UserEntity;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public final class ReportSpecification {

    private ReportSpecification() {}

    public static Specification<ReportEntity> from(ReportFilterRequest filter) {
        return (root, query, cb) -> {

            if (Long.class != query.getResultType() && long.class != query.getResultType()) {
                root.fetch("reporter", JoinType.LEFT);
                root.fetch("targetUser", JoinType.LEFT);
                root.fetch("targetRoom", JoinType.LEFT);
            }
            
            List<Predicate> predicates = new ArrayList<>();
            Join<ReportEntity, UserEntity> reporter = null;
            Join<ReportEntity, UserEntity> targetUser = null;
            Join<ReportEntity, RoomEntity> targetRoom = null;

            String search = filter.search();
            if (hasText(filter.search())) {
                reporter = root.join("reporter", JoinType.LEFT);
                targetUser = root.join("targetUser", JoinType.LEFT);
                targetRoom =  root.join("targetRoom", JoinType.LEFT);

                String keyword = "%" + search.trim().toLowerCase() + "%";
                Predicate reporterEmail = cb.like(cb.lower(reporter.get("email")), keyword);
                Predicate targetUsername = cb.like(cb.lower(targetUser.get("username")), keyword);
                Predicate targetRoomname = cb.like(cb.lower(targetRoom.get("name")), keyword);
                predicates.add(cb.or(reporterEmail, targetUsername, targetRoomname));
            }
            ReportStatusEnums status = filter.status();
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }

            ReportTypeEnums type = filter.reportType();
            if (type != null) {
                predicates.add(cb.equal(root.get("reportType"), type));
            }

            ReportSeverityEnums severity = filter.severity();
            if (severity != null) {
                predicates.add(cb.equal(root.get("severity"), severity));
            }

            LocalDateTime fromDate = filter.fromDate();
            if (fromDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(
                        root.get("createdAt"), fromDate));
            }

            LocalDateTime toDate = filter.toDate();
            if (toDate != null) {
                predicates.add(cb.lessThanOrEqualTo(
                        root.get("createdAt"), toDate.plusDays(1)));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private static boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}