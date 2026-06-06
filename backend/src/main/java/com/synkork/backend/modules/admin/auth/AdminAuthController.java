package com.synkork.backend.modules.admin.auth;

import com.synkork.backend.modules.admin.changePassword.PasswordResetRequestService;
import com.synkork.backend.modules.admin.changePassword.enums.PasswordResetStatusEnum;
import com.synkork.backend.modules.auth.dto.LoginRequest;
import com.synkork.backend.modules.auth.dto.PasswordResetVerifyRequest;
import com.synkork.backend.modules.auth.dto.ResetPasswordRequest;
import com.synkork.backend.modules.verification.VerificationEntity;
import com.synkork.backend.modules.verification.VerificationService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/manage/auth")
public class AdminAuthController {

    @Autowired
    private AdminAuthService authService;

    @Autowired
    private VerificationService verificationService;

    @Autowired
    private PasswordResetRequestService  passwordResetRequestService;

    @GetMapping("/check")
    public ResponseEntity<?> checkAuth() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        Map<String, Object> response = new HashMap<>();
        response.put("username", userDetails.getUsername());
        response.put("roles", userDetails.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList()));

        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@Valid @RequestBody LoginRequest request, HttpServletResponse response) {
        try {
            String accessToken = authService.login(request, response);
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(accessToken);
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getReason());
        }
    }

    @PostMapping("/reset-password-request")
    public ResponseEntity<String> requestPasswordReset(@RequestBody ResetPasswordRequest request) {
        String verifyCode =  passwordResetRequestService.createRequest(request.email());
        return ResponseEntity.ok(verifyCode);
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> verifyAccount(@Valid @RequestBody PasswordResetVerifyRequest request) {
        VerificationEntity verify = verificationService.verifyOtp(request.email(), request.otpCode());
        passwordResetRequestService.buildChangePasswordRequest(verify.getUser(), request.password());
        return ResponseEntity.ok("Xác thực tài khoản thành công");
    }
}
