package com.synkork.backend.modules.admin.report;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.synkork.backend.modules.report.ReportEntity;

public interface AdminReportRepository extends JpaRepository<ReportEntity, UUID>, JpaSpecificationExecutor<ReportEntity>{
    
}
