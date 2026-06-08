package com.synkork.backend.modules.admin.log;

import com.synkork.backend.modules.admin.log.dtos.AuditLogDetailResponse;
import com.synkork.backend.modules.admin.log.dtos.AuditLogResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/manage/admin/logs")
public class AuditLogController {

    @Autowired
    private AuditLogService auditLogService;

    @GetMapping("")
    public Page<AuditLogResponse> findAll(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false) Long workspaceId,
            @RequestParam(required = false) String actorEmail,

            // Định dạng truyền lên: yyyy-MM-dd'T'HH:mm:ss (Ví dụ: 2026-06-06T00:00:00)
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate,

            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<AuditLogEntity> list = auditLogService.findAll(search, action, entityType, workspaceId, actorEmail, fromDate, toDate, pageable);

        return list.map(AuditLogResponse::new);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AuditLogDetailResponse> findOne(@PathVariable String id) {
        AuditLogEntity entity = auditLogService.findById(id);
        return ResponseEntity.ok(new AuditLogDetailResponse(entity));
    }
}