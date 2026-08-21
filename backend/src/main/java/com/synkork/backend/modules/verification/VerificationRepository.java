package com.synkork.backend.modules.verification;

import com.synkork.backend.modules.user.UserEntity;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface VerificationRepository extends JpaRepository<VerificationEntity, UUID> {
    Optional<VerificationEntity> findByUserAndType(UserEntity user, VerifyTypeEnum type);

    Optional<VerificationEntity> findByUser_Email(String userEmail);

    @Modifying
    @Transactional
    @Query("DELETE FROM VerificationEntity v WHERE v.expiredAt < :now")
    void deleteExpiredVerifications();
}
