package com.synkork.backend.modules.admin.report;

import com.synkork.backend.common.utils.EmailService;
import com.synkork.backend.modules.admin.report.dtos.ReportDTO;
import com.synkork.backend.modules.admin.report.dtos.ReportFilterRequest;
import com.synkork.backend.modules.admin.report.dtos.ReportUpdateStatusRequest;
import com.synkork.backend.modules.report.ReportEntity;
import com.synkork.backend.modules.report.ReportRepository;
import com.synkork.backend.modules.report.enums.ReportStatusEnums;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class AdminReportService {
    @Autowired
    private AdminReportRepository adminReportRepository;

    @Autowired
    private EmailService emailService;

    public List<ReportDTO> getAllReports() {
        return adminReportRepository.findAll()
                .stream()
                .map(ReportDTO::new)
                .toList();
    }

    public ReportDTO getReportById(UUID id) {
        ReportEntity entity = adminReportRepository.findById(id).orElseThrow(() -> new RuntimeException("Report không tồn tại"));

        return new ReportDTO(entity);
    }

    public Page<ReportEntity> getFilteredReports(ReportFilterRequest request) {

        request.validate(); // validate dateFrom and dateTo

        Specification<ReportEntity> spec = ReportSpecification.from(request);
 
        Pageable pageable = PageRequest.of(
                request.getPage(),
                request.getSize(),
                Sort.by(Sort.Direction.DESC, "createdAt")
        );
 
        return adminReportRepository.findAll(spec, pageable);
    }
 
    public ReportEntity updateReportStatus(UUID reportId, ReportUpdateStatusRequest request) {
        ReportEntity report = adminReportRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException("Report không tồn tại: " + reportId));

        ReportStatusEnums newStatus = request.status();

        if (report.getStatus() == ReportStatusEnums.RESOLVED
                || report.getStatus() == ReportStatusEnums.DISMISSED) {
            throw new RuntimeException("Report này đã được xử lý xong, không thể thay đổi trạng thái");
        }

        report.setStatus(newStatus);
        ReportEntity savedReport = adminReportRepository.save(report);

        if (newStatus == ReportStatusEnums.RESOLVED || newStatus == ReportStatusEnums.DISMISSED) {
            emailService.sendReportResolvedEmail(
                    savedReport.getReporter().getEmail(),
                    savedReport.getReporter().getDisplayName(),
                    request.note(),
                    newStatus
            );
        }

        return savedReport;
    }

    public void deleteReport(UUID reportId) {
        ReportEntity report = adminReportRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException("Report không tồn tại: " + reportId));
        adminReportRepository.delete(report);
    }
}
