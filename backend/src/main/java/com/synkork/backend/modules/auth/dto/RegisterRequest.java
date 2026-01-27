package com.synkork.backend.modules.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {
  private String firstName;
  private String lastName;
  private String username;
  private String email;
  private String password;
}
