package com.synkork.backend.modules.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record PasswordResetVerifyRequest(
        @NotBlank(message = "Email không được để trống")
        @Email
        String email,

        @NotBlank(message = "Mã OTP không được để trống")
        @Size(min = 6, max = 6, message = "Mã OTP phải có 6 chữ số")
        @Pattern(regexp = "\\d{6}", message = "Mã OTP chỉ được chứa chữ số")
        String otpCode,

        @NotBlank(message = "Mật khẩu mới không được để trống")
        @Size(min = 6, message = "Mật khẩu dài ít nhất 6 kĩ tự")
        String password
) {}