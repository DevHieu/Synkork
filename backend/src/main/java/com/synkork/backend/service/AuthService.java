package com.synkork.backend.service;

import org.springframework.stereotype.Service;

import com.synkork.backend.dto.LoginRequest;

// Xem bên UserService để hiểu thêm (Bố m ghi hết bên đất r đó)
@Service
public class AuthService {

  public void login(LoginRequest request) {
    System.out.println("Logging in user: " + request.getEmail());
    System.out.println("Password: " + request.getPassword());
  }
}
