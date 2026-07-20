package com.synkork.backend.modules.payment.repository;

import com.synkork.backend.modules.payment.entity.UserSubscriptionEntity; 
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserSubscriptionRepository 
        extends JpaRepository<UserSubscriptionEntity, UUID>, JpaSpecificationExecutor<UserSubscriptionEntity> {


    Optional<UserSubscriptionEntity>
    findByUserIdAndCurrentTrue(UUID userId);

    Optional<UserSubscriptionEntity> findByInvoiceId(UUID invoiceId);

}
