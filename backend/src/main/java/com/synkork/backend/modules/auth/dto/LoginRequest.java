package com.synkork.backend.modules.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Các lớp DTO (Data Transfer Object) dùng để chuyển dữ liệu giữa client và server

// Ví dụ: Ở client chỉ gửi username và password để đăng nhập
// Nhưng server lại cần đầy đủ thông tin user nên ta sẽ dùng DTO để chuyển đổi dữ liệu

// Xem việc xài DTO lúc nào thì qua file AuthController để xem thêm (Bố note bên đó dài lắm đấy)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {

  @NotBlank(message = "Username không được để trống")
  private String username;

  @NotBlank(message = "Mật khẩu mới không được để trống")
  @Size(min = 6, message = "Mật khẩu dài ít nhất 6 kĩ tự")
  private String password;
}
