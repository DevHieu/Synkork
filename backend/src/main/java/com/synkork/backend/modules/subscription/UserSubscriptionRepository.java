package com.synkork.backend.modules.subscription;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.UUID;

@Repository
public interface UserSubscriptionRepository extends JpaRepository<UserSubscriptionEntity, UUID> {
    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
}
