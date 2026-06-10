package com.synkork.backend.modules.admin.log;

import com.synkork.backend.common.response.ApiResponse;
import com.synkork.backend.common.response.DeleteResponse;
import com.synkork.backend.common.response.PageMeta;
import com.synkork.backend.modules.admin.log.dtos.AuditLogDetailResponse;
import com.synkork.backend.modules.admin.log.dtos.AuditLogFilterRequest;
import com.synkork.backend.modules.admin.log.dtos.AuditLogRequest;
import com.synkork.backend.modules.admin.log.dtos.AuditLogResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/manage/admin/logs")
public class AuditLogController {

    @Autowired
    private AuditLogService auditLogService;

    @GetMapping("")
    public ApiResponse<List<AuditLogResponse>> findAll(
            @Valid @ModelAttribute AuditLogFilterRequest request
    ) {
        Page<AuditLogResponse> list = auditLogService.findAll(request).map(AuditLogResponse::new);

        return ApiResponse.success("Get log list successfully", list.getContent(), PageMeta.from(list));
    }

    @GetMapping("/{id}")
    public ApiResponse<AuditLogDetailResponse> findOne(@PathVariable String id) {
        AuditLogEntity entity = auditLogService.findById(id);
        return ApiResponse.success("Get log detail successfully", new AuditLogDetailResponse(entity));
    }

    @PostMapping()
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<AuditLogDetailResponse> create(@RequestBody AuditLogRequest request) {
        AuditLogEntity entity = auditLogService.createLog(request);
        return ApiResponse.success("Create log successfully", new AuditLogDetailResponse(entity));
    }

    @PutMapping("/{id}")
    public ApiResponse<AuditLogDetailResponse> update(@PathVariable String id, @RequestBody AuditLogRequest request) {
        AuditLogEntity entity = auditLogService.updateLog(id, request);
        return ApiResponse.success("Update log successfully", new AuditLogDetailResponse(entity));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable String id) {
        auditLogService.deleteLog(id);
        return ApiResponse.success("Log deleted successfully", null);
    }
}