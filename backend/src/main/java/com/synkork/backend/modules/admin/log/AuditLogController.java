package com.synkork.backend.modules.admin.log;

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
    public ResponseEntity<Page<AuditLogEntity>> findAll(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false) String workspaceId,
            @RequestParam(required = false) String actorEmail,

            // Định dạng truyền lên: yyyy-MM-dd'T'HH:mm:ss (Ví dụ: 2026-06-06T00:00:00)
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate,

            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        return ResponseEntity.ok(auditLogService.findAll(search, action, entityType,  workspaceId, actorEmail, fromDate, toDate, pageable));
    }

    @GetMapping("{id}")
    public ResponseEntity<AuditLogEntity> findOne(@PathVariable String id) {
        return ResponseEntity.ok(auditLogService.findById(id));
    }
}