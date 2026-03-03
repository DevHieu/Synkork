package com.synkork.backend.modules.auth;

import com.synkork.backend.modules.auth.dto.JwtResponse;
import com.synkork.backend.security.JwtService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.synkork.backend.modules.auth.dto.LoginRequest;
import com.synkork.backend.modules.auth.dto.RegisterRequest;
import com.synkork.backend.modules.user.UserEntity;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PostMapping;

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
  public ResponseEntity<JwtResponse> login(@Valid @RequestBody LoginRequest request) {

    JwtResponse jwtToken = authService.login(request);
    return ResponseEntity.status(HttpStatus.ACCEPTED).body(jwtToken);

    // ResponseEntity.status() để tùy chỉnh mã trạng thái HTTP trả về chứ không  có mỗi .ok()
  }

  @PostMapping("/register")
  public ResponseEntity<JwtResponse> register(@Valid @RequestBody RegisterRequest request) {
    JwtResponse jwtToken = authService.register(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(jwtToken);
  }

  @PostMapping("/refresh")
  public ResponseEntity<String> refreshToken(@Valid @RequestBody String refreshToken) {
    String username = jwtService.extractUserName(refreshToken);

    if (username != null && jwtService.validateRefreshToken(refreshToken)) {
      String newAccessToken = jwtService.generateToken(username, "ACCESS");

      return ResponseEntity.ok(newAccessToken);
    }

    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Refresh token is invalid or expired");
  }
}
