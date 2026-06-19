package com.synkork.backend.modules.admin.users;

import com.synkork.backend.modules.user.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface UserAdminRepository extends JpaRepository<UserEntity, UUID>, JpaSpecificationExecutor<UserEntity> {
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
}