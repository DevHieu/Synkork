package com.synkork.backend.modules.user;

//import com.synkork.backend.modules.statistics.StatisticsEntity;
//import com.synkork.backend.modules.statistics.dtos.CountByDate;
import com.synkork.backend.modules.user.enums.PlanEnum;
import com.synkork.backend.modules.user.enums.RoleEnum;
import com.synkork.backend.modules.user.enums.UserStatusEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

// Mỗi entity sẽ có một repository tương ứng để thao tác với database
// JpaRepository cung cấp các phương thức CRUD cơ bản
// Mỗi repository nên được đánh dấu với @Repository để Spring có thể quản lý nó như một bean
// Và mỗi repository nên mở rộng JpaRepository với entity tương ứng và kiểu dữ liệu của khóa chính

@Repository
public interface UserRepository extends JpaRepository<UserEntity, UUID> {
    // Repo này được dùng để lấy user thật khi đồng bộ dữ liệu chat và calendar.
    Optional<UserEntity> findByUsername(String username); // Optional to handle user not found case

    Optional<UserEntity> findByEmail(String email);

    Optional<UserEntity> findByUsernameOrEmail(String username, String email);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    long countByRole(RoleEnum role);

    long countByCreatedAtBetweenAndRole(LocalDateTime createdAtAfter, LocalDateTime createdAtBefore, RoleEnum role);

    long countByRoleAndStatus(RoleEnum role, UserStatusEnum status);

    long countByRoleAndCurrentPlan(RoleEnum role, PlanEnum currentPlan);

    List<UserEntity> findByPlanExpiresAtBetween(LocalDateTime now, LocalDateTime localDateTime);

    @Modifying
    @Query("UPDATE UserEntity u SET u.currentPlan = :plan, u.planExpiresAt = null WHERE u.planExpiresAt < :now")
    void resetExpiredUsersToPlan(@Param("plan") PlanEnum plan, @Param("now") LocalDateTime now);

    @Query("SELECT u.email FROM UserEntity u WHERE u.planExpiresAt < :now")
    List<String> findEmailByPlanExpiresAtAfter(LocalDateTime now);

    @Query("SELECT COUNT(u) FROM UserEntity u WHERE u.currentPlan != :freePlan AND (u.planExpiresAt IS NULL OR u.planExpiresAt > :now)")
    long countActiveSubscriptions(@Param("freePlan") PlanEnum freePlan, @Param("now") LocalDateTime now);

    List<UserEntity> findTop10ByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(String username, String email);

    @Query("""
        SELECT CAST(u.createdAt AS LocalDate), COUNT(u)
        FROM UserEntity u
        WHERE u.createdAt >= :from AND u.role = :role
        GROUP BY CAST(u.createdAt AS LocalDate)
        ORDER BY CAST(u.createdAt AS LocalDate)
    """)
    List<Object[]> findDailyNewUserCounts(
            @Param("from") LocalDateTime from,
            @Param("role") RoleEnum role);
}

