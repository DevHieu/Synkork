package com.synkork.backend.modules.admin.users;

import com.synkork.backend.modules.admin.users.dtos.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/manage/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminUserService adminUserService;

    // GET /manage/admin/users?keyword=&role=&status=&page=0&size=20
    @GetMapping
    public ResponseEntity<UserPageResponse> getUsers(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {

        return ResponseEntity.ok(
                adminUserService.getUsers(keyword, role, status, PageRequest.of(page, size))
        );
    }

    // GET /manage/admin/users/:id
    @GetMapping("/{id}")
    public ResponseEntity<AdminUserResponse> getUserById(@PathVariable UUID id) {
        return ResponseEntity.ok(adminUserService.getUserById(id));
    }

    // POST /manage/admin/users
    @PostMapping
    public ResponseEntity<AdminUserResponse> createUser(
            @Valid @RequestBody CreateUserRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(adminUserService.createUser(request));
    }

    // PATCH /manage/admin/users/:id
    @PatchMapping("/{id}")
    public ResponseEntity<AdminUserResponse> updateUser(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateUserRequest request) {
        return ResponseEntity.ok(adminUserService.updateUser(id, request));
    }

    // DELETE /manage/admin/users/:id
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID id) {
        adminUserService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    // PATCH /manage/admin/users/:id/status
    @PatchMapping("/{id}/status")
    public ResponseEntity<AdminUserResponse> updateStatus(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateStatusRequest request) {
        return ResponseEntity.ok(adminUserService.updateStatus(id, request));
    }
}