package com.synkork.backend.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.synkork.backend.dto.LoginRequest;
import com.synkork.backend.service.AuthService;
import com.synkork.backend.utils.ApiResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;

// Chưa biết Controller là gì thì cút qua phần UserController mà xem
@RestController
@RequestMapping("/auth")
public class AuthController {

  @Autowired
  AuthService authService;

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
  @GetMapping("/login")
  public ResponseEntity<ApiResponse<String>> login(@RequestBody LoginRequest request) {
    authService.login(request);

    // ResponseEntity.status() để tùy chỉnh mã trạng thái HTTP trả về chứ không chỉ
    // có mỗi .ok()
    return ResponseEntity.status(HttpStatus.ACCEPTED).body(
        new ApiResponse<>(
            false,
            "Login successfuly ", null));
  }

}
