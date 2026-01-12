package com.synkork.backend.entity;

import com.synkork.backend.entity.enums.ProviderEnum;
import com.synkork.backend.entity.enums.UserStatusEnum;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User extends BaseEntity { // extend từ BaseEntity để kế thừa id, createdAt, updatedAt
  @Column(unique = true, nullable = false)
  private String username;

  @Column(unique = true)
  private String email;

  private String password;

  private String fullname;

  private String avatarUrl;

  @Enumerated(EnumType.STRING) // Dùng enum thay vì String để tránh lỗi khi đổi tên role
  private com.synkork.backend.entity.enums.RoleEnum role;

  @Enumerated(EnumType.STRING) // Dùng enum thay vì String để tránh lỗi khi đổi tên status
  private UserStatusEnum status;

  private ProviderEnum provider;
}
