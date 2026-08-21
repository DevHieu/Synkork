package com.synkork.backend.modules.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {
  @NotBlank(message = "Firstname must not be blank")
  private String firstName;
  @NotBlank(message = "Lastname must not be blank")
  private String lastName;
  @NotBlank(message = "Username must not be blank")
  private String username;
  @NotBlank(message = "Email must not be blank")
  @Email
  private String email;
  @NotBlank(message = "Password must not be blank")
  @Size(min = 6, message = "Mật khẩu dài ít nhất 6 kĩ tự")
  private String password;
}
