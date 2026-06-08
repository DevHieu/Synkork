package com.synkork.backend.modules.admin.report;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.synkork.backend.modules.report.ReportService;
import com.synkork.backend.modules.report.dtos.ReportDTO;
import com.synkork.backend.modules.report.dtos.ReportFilterRequest;
import com.synkork.backend.modules.report.dtos.ReportPageResponse;
import com.synkork.backend.modules.report.dtos.ReportUpdateStatusRequest;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/manage/reports")
public class AdminReportController {
    @Autowired
    private ReportService reportService;
    
    @GetMapping
    public ResponseEntity<ReportPageResponse> getReports(@ModelAttribute ReportFilterRequest filter) {
        return ResponseEntity.ok(reportService.getFilteredReports(filter));
    }
 
    @PatchMapping("/{id}/status")
    public ResponseEntity<ReportDTO> updateStatus(@PathVariable UUID id, @Valid @RequestBody ReportUpdateStatusRequest request) {
        return ResponseEntity.ok(reportService.updateReportStatus(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReport(@PathVariable UUID id) {
        reportService.deleteReport(id);
        return ResponseEntity.noContent().build();
    }
}
