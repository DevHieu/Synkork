package com.synkork.backend.modules.admin.log;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLogEntity, UUID>, JpaSpecificationExecutor<AuditLogEntity> {
}
