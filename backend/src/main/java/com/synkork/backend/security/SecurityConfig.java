package com.synkork.backend.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

// Đây sẽ là lớp cấu hình bảo mật cho ứng dụng
@EnableMethodSecurity
@Configuration
@EnableWebSecurity
public class SecurityConfig {
  // Cấu hình bảo mật sẽ được thêm vào đây trong tương lai
}
