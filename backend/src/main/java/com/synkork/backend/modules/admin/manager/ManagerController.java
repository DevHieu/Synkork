package com.synkork.backend.modules.admin.manager;

import com.synkork.backend.common.response.ApiResponse;
import com.synkork.backend.common.response.PageMeta;
import com.synkork.backend.modules.admin.manager.dto.*;
import com.synkork.backend.modules.admin.users.dtos.AdminUserResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/manage/admin/manager")
@RequiredArgsConstructor
public class ManagerController {

    private final ManagerService managerService;

    @GetMapping
    public ApiResponse<List<ManagerResponse>> getManagers(@Valid @ModelAttribute ManagerFilterRequest filter) {
        Page<ManagerResponse> list = managerService.getManagers(filter).map(ManagerResponse::from);

        return ApiResponse.success(
                "Get manager list successfully",
                list.getContent(),
                PageMeta.from(list)
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
    public ResponseEntity<Map<String, String>> lockManager(@PathVariable UUID id) {
        return ResponseEntity.ok(managerService.lockManager(id));
    }
}
