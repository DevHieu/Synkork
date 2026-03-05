package com.synkork.backend.modules.auth;

import com.synkork.backend.modules.auth.dto.JwtResponse;
import com.synkork.backend.modules.auth.dto.LoginRequest;
import com.synkork.backend.modules.auth.dto.RegisterRequest;
import com.synkork.backend.security.JwtService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// Chưa biết Controller là gì thì cút qua phần UserController mà xem
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    AuthService authService;

    @Autowired
    JwtService jwtService;

    /*
     * Ví dụ trường hợp cần 1 DTO
     *
     * Trường hợp bên client gửi 1 object login gồm email và password qua body để
     * đăng nhập (Không xài RequestParam vì bảo mật kém và ko phù hợp với object nếu
     * to hơn)
     *
     * Mà nhận bằng User entity thì ko đúng vì User entity có thể có nhiều trường
     * hơn (role, createdAt, updatedAt,...)
     *
     * Nên ta sẽ tạo 1 DTO LoginRequest chỉ gồm email và password để nhận dữ liệu từ
     * client
     */
    @PostMapping("/login")
    public ResponseEntity<String> login(@Valid @RequestBody LoginRequest request, HttpServletResponse response) {

        String accessToken = authService.login(request, response);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(accessToken);

        // ResponseEntity.status() để tùy chỉnh mã trạng thái HTTP trả về chứ không  có mỗi .ok()
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody RegisterRequest request, HttpServletResponse response) {
        String accessToken = authService.register(request, response);
        return ResponseEntity.status(HttpStatus.CREATED).body(accessToken);
    }

    @PostMapping("/refresh")
    public ResponseEntity<String> refreshToken(@CookieValue("refresh_token") String refreshToken,
                                               HttpServletResponse response) {
        String username = jwtService.extractUserName(refreshToken);
        System.out.println("Refresh Token: " + username);
        if (username != null && jwtService.validateRefreshToken(refreshToken)) {
            System.out.println("generating");
            String newAccessToken = jwtService.generateToken(username, "ACCESS");

            String newRefreshToken = jwtService.generateToken(username, "REFRESH");

            jwtService.saveRefreshToken(newRefreshToken, response);

            return ResponseEntity.status(HttpStatus.CREATED).body(newAccessToken);
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Refresh token is invalid or expired");
    }

}
