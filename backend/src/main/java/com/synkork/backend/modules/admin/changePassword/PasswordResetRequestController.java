package com.synkork.backend.modules.admin.changePassword;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/manage/admin/change-password-request")
public class PasswordResetRequestController {

    @Autowired
    private PasswordResetRequestService passwordResetService;

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