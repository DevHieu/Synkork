package com.synkork.backend.modules.admin.users;

import com.synkork.backend.modules.admin.users.dtos.*;
import com.synkork.backend.modules.user.UserEntity;
import com.synkork.backend.modules.user.enums.RoleEnum;
import com.synkork.backend.modules.user.enums.UserStatusEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final UserAdminRepository userAdminRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired(required = false)
    private JavaMailSender mailSender;

    // ── GET LIST với search + filter + pagination ─────────────────────
    @Transactional(readOnly = true)
    public UserPageResponse getUsers(String keyword, String role, String status, Pageable pageable) {
        // Convert String → Enum, null nếu không truyền
        RoleEnum roleEnum = null;
        if (role != null && !role.isBlank()) {
            try { roleEnum = RoleEnum.valueOf(role.toUpperCase()); }
            catch (IllegalArgumentException ignored) {}
        }

        UserStatusEnum statusEnum = null;
        if (status != null && !status.isBlank()) {
            try { statusEnum = UserStatusEnum.valueOf(status.toUpperCase()); }
            catch (IllegalArgumentException ignored) {}
        }

        String kw = (keyword != null && !keyword.isBlank()) ? keyword.trim() : null;

        Page<UserEntity> page = userAdminRepository.findWithFilters(kw, roleEnum, statusEnum, pageable);

        List<AdminUserResponse> content = page.getContent()
                .stream()
                .map(AdminUserResponse::from)
                .collect(Collectors.toList());

        return UserPageResponse.builder()
                .content(content)
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }

    // ── GET BY ID ─────────────────────────────────────────────────────
    @Transactional(readOnly = true)
    public AdminUserResponse getUserById(UUID id) {
        return AdminUserResponse.from(findOrThrow(id));
    }

    // ── CREATE ────────────────────────────────────────────────────────
    @Transactional
    public AdminUserResponse createUser(CreateUserRequest req) {
        if (userAdminRepository.existsByEmail(req.getEmail()))
            throw new IllegalArgumentException("Email đã được sử dụng: " + req.getEmail());
        if (userAdminRepository.existsByUsername(req.getUsername()))
            throw new IllegalArgumentException("Username đã được sử dụng: " + req.getUsername());

        String displayName = (req.getFirstName() + " " + req.getLastName()).trim();
        String tempPassword = UUID.randomUUID().toString().substring(0, 8);

        UserEntity user = new UserEntity();
        user.setUsername(req.getUsername());
        user.setEmail(req.getEmail());
        user.setDisplayName(displayName);
        user.setPassword(passwordEncoder.encode(tempPassword));
        user.setRole(RoleEnum.valueOf(req.getRole().toUpperCase()));
        user.setStatus(UserStatusEnum.valueOf(req.getStatus().toUpperCase()));

        UserEntity saved = userAdminRepository.save(user);
        sendWelcomeEmail(saved.getEmail(), saved.getUsername(), tempPassword);
        return AdminUserResponse.from(saved);
    }

    // ── UPDATE ────────────────────────────────────────────────────────
    @Transactional
    public AdminUserResponse updateUser(UUID id, UpdateUserRequest req) {
        UserEntity user = findOrThrow(id);

        if (req.getDisplayName() != null)
            user.setDisplayName(req.getDisplayName());

        if (req.getEmail() != null && !req.getEmail().equals(user.getEmail())) {
            if (userAdminRepository.existsByEmail(req.getEmail()))
                throw new IllegalArgumentException("Email đã được sử dụng: " + req.getEmail());
            user.setEmail(req.getEmail());
        }

        if (req.getRole() != null)
            user.setRole(RoleEnum.valueOf(req.getRole().toUpperCase()));

        if (req.getStatus() != null)
            user.setStatus(UserStatusEnum.valueOf(req.getStatus().toUpperCase()));

        return AdminUserResponse.from(userAdminRepository.save(user));
    }

    // ── DELETE ────────────────────────────────────────────────────────
    @Transactional
    public void deleteUser(UUID id) {
        userAdminRepository.delete(findOrThrow(id));
    }


    // ── UPDATE STATUS ─────────────────────────────────────────────────
    @Transactional
    public AdminUserResponse updateStatus(UUID id, UpdateStatusRequest req) {
        UserEntity user = findOrThrow(id);
        user.setStatus(UserStatusEnum.valueOf(req.getStatus().toUpperCase()));
        return AdminUserResponse.from(userAdminRepository.save(user));
    }

    // ── HELPERS ───────────────────────────────────────────────────────

    private UserEntity findOrThrow(UUID id) {
        return userAdminRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy user: " + id));
    }

    private void sendWelcomeEmail(String email, String username, String tempPassword) {
        if (mailSender == null) return;
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setTo(email);
            msg.setSubject("[Synkork] Tài khoản của bạn đã được tạo");
            msg.setText(String.format(
                    "Xin chào %s,\n\nMật khẩu tạm thời: %s\n\nVui lòng đổi mật khẩu sau khi đăng nhập.",
                    username, tempPassword
            ));
            mailSender.send(msg);
        } catch (Exception ignored) {}
    }

}