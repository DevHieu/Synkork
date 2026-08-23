package com.synkork.backend.modules.auth;

import com.synkork.backend.modules.auth.dto.LoginRequest;
import com.synkork.backend.modules.auth.dto.RegisterRequest;
import com.synkork.backend.modules.auth.dto.PasswordResetVerifyRequest;
import com.synkork.backend.modules.user.enums.RoleEnum;
import com.synkork.backend.modules.verification.VerificationService;
import com.synkork.backend.security.JwtService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

// Chưa biết Controller là gì thì cút qua phần UserController mà xem
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    AuthService authService;

    @Autowired
    JwtService jwtService;

    @Autowired
    VerificationService verificationService;

    @GetMapping("/check")
    public ResponseEntity<?> checkAuth() {
        return ResponseEntity.ok().build();
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@Valid @RequestBody LoginRequest request, HttpServletResponse response) {
        String accessToken = authService.login(request, response);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(Map.of("accessToken", accessToken));
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", "Đăng ký thành công, vui lòng kiểm tra email để xác thực tài khoản"));
    }

    @PostMapping("/refresh")
    public ResponseEntity<Map<String, String>> refreshToken(@CookieValue("refreshToken") String refreshToken,
                                               HttpServletResponse response) {
        String username = jwtService.extractUserName(refreshToken);
        String userId = jwtService.extractClaim(refreshToken, claims -> claims.get("userId", String.class));
        RoleEnum role = RoleEnum.valueOf(jwtService.extractClaim(refreshToken, claims -> claims.get("role", String.class)));
        if (username != null && jwtService.validateRefreshToken(refreshToken)) {
            System.out.println("generating");
            String newAccessToken = jwtService.generateToken(userId, username, role, "ACCESS");

            String newRefreshToken = jwtService.generateToken(userId, username, role, "REFRESH");

            jwtService.saveRefreshToken(newRefreshToken, response);

            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("accessToken",newAccessToken));
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Refresh token is invalid or expired"));
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(HttpServletResponse response) {
        System.out.println("Logout");

        Cookie cookie = new Cookie("refreshToken", null);
        cookie.setMaxAge(0);
        cookie.setPath("/");
        cookie.setHttpOnly(true);

        response.addCookie(cookie);
        return ResponseEntity.status(HttpStatus.OK).body(Map.of("message","Logged out"));
    }

    @GetMapping("/verify")
    public ResponseEntity<Map<String, String>> verifyAccount(@RequestParam String token) {
        verificationService.verifyAccountRegister(token);
        return ResponseEntity.ok(Map.of("message","Xác thực tài khoản thành công"));
    }

    @PostMapping("/request-password-reset")
    public ResponseEntity<Map<String, String>> forgotPassword(@RequestBody Map<String, String> body) {
        authService.sendRequestPasswordReset(body.get("email"));
        return ResponseEntity.ok(Map.of("message","OTP đã được gửi đến email của bạn"));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(@Valid @RequestBody PasswordResetVerifyRequest request) {
        authService.resetPassword(request);
        return ResponseEntity.ok(Map.of("message","Đặt lại mật khẩu thành công"));
    }
}
