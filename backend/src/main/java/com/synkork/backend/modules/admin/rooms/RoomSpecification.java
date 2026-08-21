package com.synkork.backend.modules.admin.rooms;

import com.synkork.backend.modules.admin.rooms.dtos.RoomFilterRequest;
import com.synkork.backend.modules.room.RoomEntity;
import com.synkork.backend.modules.room.enums.RoomTypeEnum;
import com.synkork.backend.modules.roomMember.RoomMemberEntity;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class RoomSpecification {

    public static Specification<RoomEntity> filter(RoomFilterRequest request) {
        return (root, query, cb) -> {
            if (Long.class != query.getResultType() && long.class != query.getResultType()) {
                root.fetch("owner", JoinType.LEFT);
                root.fetch("roomMembers", JoinType.LEFT);
            }

            List<Predicate> predicates = new ArrayList<>();

            predicates.add(cb.equal(root.get("type"), RoomTypeEnum.GROUP));

            if (hasText(request.search())) {
                predicates.add(cb.like(
                        cb.lower(root.get("name")),
                        "%" + request.search().trim().toLowerCase() + "%"
                ));
            }

            if (request.status() != null) {
                predicates.add(cb.equal(root.get("status"), request.status()));
            }

            if (request.dateFrom() != null) {
                predicates.add(cb.greaterThanOrEqualTo(
                        root.get("createdAt"),
                        request.dateFrom().atStartOfDay()
                ));
            }

            if (request.dateTo() != null) {
                predicates.add(cb.lessThanOrEqualTo(
                        root.get("createdAt"),
                        request.dateTo().atTime(23, 59, 59)
                ));
            }

            if (request.minMembers() != null || request.maxMembers() != null) {
                Subquery<Long> memberCountSubquery = query.subquery(Long.class);
                var memberRoot = memberCountSubquery.from(RoomMemberEntity.class);

                memberCountSubquery.select(cb.count(memberRoot))
                        .where(cb.equal(memberRoot.get("room"), root));

                if (request.minMembers() != null) {
                    predicates.add(cb.greaterThanOrEqualTo(
                            memberCountSubquery,
                            request.minMembers().longValue()
                    ));
                }

                if (request.maxMembers() != null) {
                    predicates.add(cb.lessThanOrEqualTo(
                            memberCountSubquery,
                            request.maxMembers().longValue()
                    ));
                }
            }

            if (request.minWarning() != null) {
                predicates.add(cb.greaterThanOrEqualTo(
                        root.get("warning"),
                        request.minWarning()
                ));
            }

            if (request.maxWarning() != null) {
                predicates.add(cb.lessThanOrEqualTo(
                        root.get("warning"),
                        request.maxWarning()
                ));
            }

            query.distinct(true);
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private static boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}