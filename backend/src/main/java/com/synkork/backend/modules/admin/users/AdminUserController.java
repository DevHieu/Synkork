package com.synkork.backend.modules.admin.users;

import com.synkork.backend.common.response.ApiResponse;
import com.synkork.backend.common.response.PageMeta;
import com.synkork.backend.modules.admin.users.dtos.*;
import com.synkork.backend.modules.user.enums.UserStatusEnum;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/manage/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminUserService adminUserService;

    @GetMapping
    public ApiResponse<List<AdminUserResponse>> getUsers(@Valid @ModelAttribute UserFilterRequest request) {
        Page<AdminUserResponse> list = adminUserService.getUsers(request).map(AdminUserResponse::from);

        return ApiResponse.success(
                "Get user list successfully",
                list.getContent(),
                PageMeta.from(list)

        );
    }

    @GetMapping("/{id}")
    public ApiResponse<AdminUserResponse> getUserById(@PathVariable UUID id) {
        return ApiResponse.success("Get user successfully", adminUserService.getUserById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<AdminUserResponse> createUser(
            @Valid @RequestBody CreateUserRequest request
    ) {
        return ApiResponse.success(
                "Create user successfully",
                adminUserService.createUser(request)
        );  
    }

    @PatchMapping("/{id}")
    public ApiResponse<AdminUserResponse> updateUser(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateUserRequest request
    ) {
        return ApiResponse.success(
                "Update user successfully",
                adminUserService.updateUser(id, request)
        );
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Map<String, String>> deleteUser(
            @PathVariable UUID id
    ) {
        return ApiResponse.success(
                "Delete user successfully",
                adminUserService.deleteUser(id)
        );
    }

    @PatchMapping("/{id}/status")
    public ApiResponse<AdminUserResponse> lockUser(@PathVariable UUID id, @Valid @RequestBody UpdateUserRequest request){
        return ApiResponse.success("Lock user successfully", adminUserService.lockUser(id, UserStatusEnum.valueOf(request.status())));
    }

    @PatchMapping("/{id}/warn")
    public ApiResponse<AdminUserResponse> warnUser(@PathVariable UUID id) {
        return ApiResponse.success("Warn user successfully", adminUserService.warnUser(id));
    }
}