package com.synkork.backend.modules.auth;

import com.synkork.backend.modules.auth.dto.LoginRequest;
import com.synkork.backend.modules.auth.dto.RegisterRequest;
import com.synkork.backend.modules.auth.dto.OtpVerifyRequest;
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
import org.springframework.web.server.ResponseStatusException;

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

    @GetMapping("/check-login")
    public ResponseEntity<?> checkAuth() {
        return ResponseEntity.ok().build();
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@Valid @RequestBody LoginRequest request, HttpServletResponse response) {

        try {
            String accessToken = authService.login(request, response);
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(accessToken);
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getReason());
        }

        // ResponseEntity.status() để tùy chỉnh mã trạng thái HTTP trả về chứ không  có mỗi .ok()
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody RegisterRequest request) {
        try {
            authService.register(request);
            return ResponseEntity.status(HttpStatus.CREATED).body("Đăng ký thành công, vui lòng kiểm tra email để xác thực tài khoản");
        } catch (ResponseStatusException e) {
            return ResponseEntity
                    .status(e.getStatusCode())
                    .body(e.getReason());
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<String> refreshToken(@CookieValue("refreshToken") String refreshToken,
                                               HttpServletResponse response) {
        String username = jwtService.extractUserName(refreshToken);
        String userId = jwtService.extractClaim(refreshToken, claims -> claims.get("userId", String.class));
        RoleEnum role = RoleEnum.valueOf(jwtService.extractClaim(refreshToken, claims -> claims.get("role", String.class)));
        if (username != null && jwtService.validateRefreshToken(refreshToken)) {
            System.out.println("generating");
            String newAccessToken = jwtService.generateToken(userId, username, role, "ACCESS");

            String newRefreshToken = jwtService.generateToken(userId, username, role, "REFRESH");

            jwtService.saveRefreshToken(newRefreshToken, response);

            return ResponseEntity.status(HttpStatus.CREATED).body(newAccessToken);
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Refresh token is invalid or expired");
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletResponse response) {
        System.out.println("Logout");

        Cookie cookie = new Cookie("refreshToken", null);
        cookie.setMaxAge(0);
        cookie.setPath("/");
        cookie.setHttpOnly(true);

        response.addCookie(cookie);
        return ResponseEntity.status(HttpStatus.OK).body("Logged out");
    }

    @GetMapping("/verify")
    public ResponseEntity<String> verifyAccount(@RequestParam String token) {
        verificationService.verifyAccountRegister(token);
        return ResponseEntity.ok("Xác thực tài khoản thành công");
    }
//
//    @PostMapping("/request-password-reset")
//    public ResponseEntity<String> forgotPassword(@RequestBody Map<String, String> body) {
//        authService.sendRequestPasswordReset(body.get("email"));
//        return ResponseEntity.ok("Link đặt lại mật khẩu đã được gửi");
//    }
//
//    @PostMapping("/reset-password")
//    public ResponseEntity<String> resetPassword(@Valid @RequestBody OtpVerifyRequest request) {
//        authService.resetPassword(request.getToken(), request.getPassword());
//        return ResponseEntity.ok("Đặt lại mật khẩu thành công");
//    }
}
