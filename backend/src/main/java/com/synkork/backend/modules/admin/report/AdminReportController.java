package com.synkork.backend.modules.admin.report;

import java.util.List;
import java.util.UUID;

import com.synkork.backend.common.response.ApiResponse;
import com.synkork.backend.common.response.PageMeta;
import com.synkork.backend.modules.admin.report.dtos.ReportResponse;
import com.synkork.backend.modules.report.ReportEntity;
import com.synkork.backend.modules.report.dtos.ReportRequestDto;
import com.synkork.backend.modules.report.enums.ReportTypeEnums;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import com.synkork.backend.modules.report.ReportService;
import com.synkork.backend.modules.admin.report.dtos.ReportFilterRequest;
import com.synkork.backend.modules.admin.report.dtos.ReportUpdateStatusRequest;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/manage/reports")
public class AdminReportController {
    @Autowired
    private AdminReportService adminReportService;

    @Autowired
    private ReportService reportService;

    @GetMapping
    public ApiResponse<List<ReportResponse>> getReports(@ModelAttribute ReportFilterRequest filter) {
        Page<ReportResponse> page = adminReportService.getFilteredReports(filter).map(ReportResponse::new);
        return ApiResponse.success("Get report list successfully", page.getContent(), PageMeta.from(page));
    }

    @GetMapping("/{id}")
    public ApiResponse<ReportResponse> getReportById(@PathVariable UUID id) {
        ReportResponse entity = adminReportService.getReportById(id);
        return ApiResponse.success("Get report detail successfully", entity);
    }

//    @PostMapping("/users")
//    @ResponseStatus(HttpStatus.CREATED)
//    public ApiResponse<ReportResponse> createUserReport(@RequestBody ReportRequestDto request) {
//        ReportEntity entity = reportService.createReport(request, ReportTypeEnums.USER);
//        return ApiResponse.success("User report created successfully", new ReportResponse(entity));
//    }
//
//    @PostMapping("/rooms")
//    @ResponseStatus(HttpStatus.CREATED)
//    public ApiResponse<ReportResponse> createRoomReport(@RequestBody ReportRequestDto request) {
//        ReportEntity entity = reportService.createReport(request, ReportTypeEnums.ROOM);
//        return ApiResponse.success("Room report created successfully", new ReportResponse(entity));
//    }

    @PatchMapping("/{id}/status")
    public ApiResponse<ReportResponse> updateStatus(@PathVariable UUID id, @Valid @RequestBody ReportUpdateStatusRequest request) {
        ReportEntity entity = adminReportService.updateReportStatus(id, request);
        return ApiResponse.success("Report status updated successfully", new ReportResponse(entity));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteReport(@PathVariable UUID id) {
        adminReportService.deleteReport(id);
        return ApiResponse.success("Report deleted successfully", null);
    }
}
