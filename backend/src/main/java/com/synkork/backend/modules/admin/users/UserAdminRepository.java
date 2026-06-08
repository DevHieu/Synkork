package com.synkork.backend.modules.admin.users;

import com.synkork.backend.modules.user.UserEntity;
import com.synkork.backend.modules.user.enums.RoleEnum;
import com.synkork.backend.modules.user.enums.UserStatusEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface UserAdminRepository extends JpaRepository<UserEntity, UUID> {

    Optional<UserEntity> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    @Query("""
        SELECT u FROM UserEntity u
        WHERE
            (:keyword IS NULL OR
                LOWER(u.username)    LIKE LOWER(CONCAT('%', :keyword, '%')) OR
                LOWER(u.email)       LIKE LOWER(CONCAT('%', :keyword, '%')) OR
                LOWER(u.displayName) LIKE LOWER(CONCAT('%', :keyword, '%'))
            )
            AND (:role   IS NULL OR u.role   = :role)
            AND (:status IS NULL OR u.status = :status)
        """)
    Page<UserEntity> findWithFilters(
            @Param("keyword") String keyword,
            @Param("role")    RoleEnum role,
            @Param("status")  UserStatusEnum status,
            Pageable pageable
    );
}