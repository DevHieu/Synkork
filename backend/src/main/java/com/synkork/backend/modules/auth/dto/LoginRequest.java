package com.synkork.backend.modules.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Các lớp DTO (Data Transfer Object) dùng để chuyển dữ liệu giữa client và server

// Ví dụ: Ở client chỉ gửi email và password để đăng nhập
// Nhưng server lại cần đầy đủ thông tin user nên ta sẽ dùng DTO để chuyển đổi dữ liệu

// Xem việc xài DTO lúc nào thì qua file AuthController để xem thêm (Bố note bên đó dài lắm đấy)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {
  private String email;
  private String password;
}
