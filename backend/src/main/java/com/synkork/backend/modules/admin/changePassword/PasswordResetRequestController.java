package com.synkork.backend.modules.admin.changePassword;

import com.synkork.backend.common.response.ApiResponse;
import com.synkork.backend.common.response.PageMeta;
import com.synkork.backend.modules.admin.changePassword.dto.PasswordResetRequestFilter;
import com.synkork.backend.modules.admin.changePassword.dto.PasswordResetRequestResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/manage/admin/change-password-request")
public class PasswordResetRequestController {

    @Autowired
    private PasswordResetRequestService passwordResetService;

    @GetMapping
    public ApiResponse<List<PasswordResetRequestResponse>> getRequests(
            @Valid @ModelAttribute PasswordResetRequestFilter filter
    ) {
        Page<PasswordResetRequestResponse> list = passwordResetService.getRequests(filter)
                .map(PasswordResetRequestResponse::from);

        return ApiResponse.success(
                "Get password reset request list successfully",
                list.getContent(),
                PageMeta.from(list)
        );
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<String> approve(@PathVariable UUID id) {
        passwordResetService.approve(id);
        return ResponseEntity.ok("Đã duyệt, mật khẩu đã được đổi");
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<String> reject(@PathVariable UUID id) {
        passwordResetService.reject(id);
        return ResponseEntity.ok("Đã từ chối yêu cầu");
    }
}
