package com.synkork.backend.modules.admin.manager;

import com.synkork.backend.modules.admin.manager.dto.CreateManagerRequest;
import com.synkork.backend.modules.admin.manager.dto.ManagerPageResponse;
import com.synkork.backend.modules.admin.manager.dto.ManagerResponse;
import com.synkork.backend.modules.admin.manager.dto.UpdateManagerRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/manage/admin")
@RequiredArgsConstructor
public class ManagerController {

    private final ManagerService managerService;

    @GetMapping
    public ResponseEntity<ManagerPageResponse> getManagers(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(
                managerService.getManagers(keyword, status, PageRequest.of(page, size))
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ManagerResponse> getManager(@PathVariable UUID id) {
        return ResponseEntity.ok(managerService.getManager(id));
    }

    @PostMapping
    public ResponseEntity<ManagerResponse> createManager(
            @Valid @RequestBody CreateManagerRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(managerService.createManager(request));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ManagerResponse> updateManager(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateManagerRequest request) {
        return ResponseEntity.ok(managerService.updateManager(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteManager(@PathVariable UUID id) {
        return ResponseEntity.ok(managerService.deleteManager(id));
    }
}
