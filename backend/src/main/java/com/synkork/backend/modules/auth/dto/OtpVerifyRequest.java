package com.synkork.backend.modules.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record OtpVerifyRequest(
        @NotBlank(message = "Token không được để trống")
        String token,

        @NotBlank(message = "Mã OTP không được để trống")
        @Size(min = 6, max = 6, message = "Mã OTP phải có 6 chữ số")
        @Pattern(regexp = "\\d{6}", message = "Mã OTP chỉ được chứa chữ số")
        String otpCode
) {}