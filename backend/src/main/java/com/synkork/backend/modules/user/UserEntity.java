package com.synkork.backend.modules.user;

import com.synkork.backend.common.base.BaseEntity;
import com.synkork.backend.modules.user.enums.ProviderEnum;
import com.synkork.backend.modules.user.enums.UserStatusEnum;

import com.synkork.backend.modules.user.enums.RoleEnum;
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
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserEntity extends BaseEntity {

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(name = "display_name")
    private String displayName;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    private String avatarUrl;
    private String avatarId;
    private String provider;

    private String role;
    private String status;
}

