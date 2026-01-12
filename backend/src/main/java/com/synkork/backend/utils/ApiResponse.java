package com.synkork.backend.utils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Lớp này dùng để chuẩn hóa cấu trúc phản hồi API
// Sử dụng khi trả data từ Controller
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApiResponse<T> {
  private boolean success;
  private String message;
  private T data;
}