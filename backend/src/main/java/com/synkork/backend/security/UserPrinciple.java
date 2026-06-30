package com.synkork.backend.security;

import com.synkork.backend.modules.user.UserEntity;
import com.synkork.backend.modules.user.enums.UserStatusEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
public class UserPrinciple implements UserDetails {

    private final UserEntity user;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singleton(
                new SimpleGrantedAuthority("ROLE_" + user.getRole().toString())
        );
    }

    @Override
    public String getPassword() {
        ;return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getEmail();
    }

    public UUID getId() {
        return user.getId();
    }

    @Override
    public boolean isEnabled() {
        return user.getStatus() == UserStatusEnum.ACTIVE;
    }

    @Override
    public boolean isAccountNonLocked() {
        return user.getStatus() != UserStatusEnum.BANNED;
    }
}

