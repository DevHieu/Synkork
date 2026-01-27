package com.synkork.backend.modules.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.synkork.backend.modules.auth.dto.LoginRequest;
import com.synkork.backend.modules.auth.dto.RegisterRequest;
import com.synkork.backend.modules.user.UserEntity;
import com.synkork.backend.modules.user.UserRepository;

// Xem bên UserService để hiểu thêm (Bố m ghi hết bên đất r đó)
@Service
public class AuthService {

  @Autowired
  UserRepository userRepository;

  public UserEntity login(LoginRequest request) {
    UserEntity user = userRepository.findByEmail(request.getEmail())
        .orElseThrow(() -> new RuntimeException("Email không tồn tại!"));

    if (!user.getPassword().equals(request.getPassword())) {
      throw new RuntimeException("Mật khẩu không đúng!");
    }

    return user;
  }

  public UserEntity register(RegisterRequest request) {
    UserEntity newUser = new UserEntity();

    newUser.setDisplayName(request.getFirstName() + " " + request.getLastName());
    newUser.setUsername(request.getUsername());
    newUser.setEmail(request.getEmail());
    newUser.setPassword(request.getPassword());

    UserEntity entity = userRepository.save(newUser);
    return entity;
  }
}
