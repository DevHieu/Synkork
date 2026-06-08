package com.synkork.backend.modules.admin.changePassword;

import com.synkork.backend.modules.admin.changePassword.enums.PasswordResetStatusEnum;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PasswordResetRequestRepository extends JpaRepository<PasswordResetRequestEntity, UUID> {
    List<PasswordResetRequestEntity> findByStatus(PasswordResetStatusEnum status);

    Optional<PasswordResetRequestEntity> findByUserEmail(String email);
}
