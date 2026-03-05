package com.synkork.backend.modules.auth;

import com.synkork.backend.modules.auth.dto.JwtResponse;
import com.synkork.backend.security.JwtService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
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

  @Autowired
  private AuthenticationManager authManager;

  @Autowired
  private JwtService jwtService;

  private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

  public String login(LoginRequest request, HttpServletResponse response) {
     Authentication authentication = authManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

      if (!authentication.isAuthenticated()) {
        throw new RuntimeException("Invalid username or password");
      }

    UserDetails userDetails = (UserDetails) authentication.getPrincipal();

    return jwtService.generateJwtToken(userDetails.getUsername(), response);
  }

  public String register(RegisterRequest request, HttpServletResponse response) {

    if (userRepository.existsByEmail(request.getEmail())) {
      throw new RuntimeException("Email already exists");
    }

    if (userRepository.existsByUsername(request.getUsername())) {
      throw new RuntimeException("Username already exists");
    }

    UserEntity newUser = UserEntity.builder()
            .displayName(request.getFirstName() + " " + request.getLastName())
            .username(request.getUsername())
            .email(request.getEmail())
            .password(encoder.encode(request.getPassword()))
            .build();

    UserEntity entity = userRepository.save(newUser);

    return jwtService.generateJwtToken(entity.getUsername(), response);
  }
}


