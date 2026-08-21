package com.synkork.backend.modules.admin.users;

import com.synkork.backend.modules.admin.statistics.dtos.UserPlanCount;
import com.synkork.backend.modules.admin.statistics.dtos.UserStatusCount;
import com.synkork.backend.modules.user.UserEntity;
import com.synkork.backend.modules.user.enums.PlanEnum;
import com.synkork.backend.modules.user.enums.RoleEnum;
import com.synkork.backend.modules.user.enums.UserStatusEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface UserAdminRepository extends JpaRepository<UserEntity, UUID>, JpaSpecificationExecutor<UserEntity> {
    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    long countByRole(RoleEnum role);

    long countByRoleAndCreatedAtBetween(RoleEnum role, LocalDateTime from, LocalDateTime to);

    long countByRoleAndCreatedAtLessThanEqual(RoleEnum role, LocalDateTime createdAt);

    long countByRoleAndStatus(RoleEnum role, UserStatusEnum status);

    long countByRoleAndCurrentPlan(RoleEnum role, PlanEnum currentPlan);

    @Query("""
            SELECT new com.synkork.backend.modules.admin.statistics.dtos.UserStatusCount(COUNT(u), u.status)
            FROM UserEntity u
            WHERE u.role = :role
              AND (:start IS NULL OR u.createdAt >= :start)
              AND (:end IS NULL OR u.createdAt <= :end)
            GROUP BY u.status
            """)
    List<UserStatusCount> countGroupByStatus(
            @Param("role") RoleEnum role,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query("""
            SELECT new com.synkork.backend.modules.admin.statistics.dtos.UserPlanCount(COUNT(u), u.currentPlan)
            FROM UserEntity u
            WHERE u.role = :role
              AND (:start IS NULL OR u.createdAt >= :start)
              AND (:end IS NULL OR u.createdAt <= :end)
            GROUP BY u.currentPlan
            """)
    List<UserPlanCount> countGroupByPlan(
            @Param("role") RoleEnum role,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );
}