package com.synkork.backend.modules.admin.workspace.rooms;

import com.synkork.backend.modules.admin.workspace.rooms.dtos.RoomFilterRequest;
import com.synkork.backend.modules.room.RoomEntity;
import com.synkork.backend.modules.room.enums.RoomTypeEnum;
import com.synkork.backend.modules.roomMember.RoomMemberEntity;
import com.synkork.backend.modules.roomMember.enums.RoomMemberStatusEnum;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class RoomSpecification {

    public static Specification<RoomEntity> filter(RoomFilterRequest request) {
        return (root, query, cb) -> {
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

            if (request.minMembers() != null && request.minMembers() > 0) {
                Subquery<Long> subquery = query.subquery(Long.class);
                var memberRoot = subquery.from(RoomMemberEntity.class);
                subquery.select(cb.count(memberRoot))
                        .where(
                                cb.equal(memberRoot.get("room"), root),
                                cb.equal(memberRoot.get("status"), RoomMemberStatusEnum.ACTIVE)
                        );
                predicates.add(cb.greaterThanOrEqualTo(subquery, (long) request.minMembers()));
            }

            query.distinct(true);
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private static boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
