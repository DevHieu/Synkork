package com.synkork.backend.modules.payment.repository;

import com.synkork.backend.modules.payment.entity.PromotionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PromotionRepository extends JpaRepository<PromotionEntity, UUID> {

    Optional<PromotionEntity> findByCodeAndActiveTrue(String code);

    @Modifying
    @Query("UPDATE PromotionEntity p SET p.usedCount = p.usedCount + 1 " +
           "WHERE p.id = :id AND (p.maxUses IS NULL OR p.usedCount < p.maxUses)")
    int incrementUsage(@Param("id") UUID id);
}