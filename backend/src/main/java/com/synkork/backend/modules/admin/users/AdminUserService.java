package com.synkork.backend.modules.admin.users;

import com.synkork.backend.modules.admin.users.dtos.AdminUserResponse;
import com.synkork.backend.modules.admin.users.dtos.CreateUserRequest;
import com.synkork.backend.modules.admin.users.dtos.UpdateUserRequest;
import com.synkork.backend.modules.admin.users.dtos.UserFilterRequest;
import com.synkork.backend.modules.user.UserEntity;
import com.synkork.backend.modules.user.enums.PlanEnum;
import com.synkork.backend.modules.user.enums.RoleEnum;
import com.synkork.backend.modules.user.enums.UserStatusEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final UserAdminRepository userAdminRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired(required = false)
    private JavaMailSender mailSender;

    public Page<UserEntity> getUsers(UserFilterRequest request) {
        request.validate();

        Specification<UserEntity> spec = UserSpecification.filter(request)
                .and((root, query, cb) -> cb.equal(root.get("role"), RoleEnum.USER));
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize());

        return userAdminRepository.findAll(spec, pageable);
    }

    public AdminUserResponse getUserById(UUID id) {
        return AdminUserResponse.from(findUserOrThrow(id));
    }

    public AdminUserResponse createUser(CreateUserRequest req) {
        if (userAdminRepository.existsByEmail(req.email())) {
            throw new IllegalArgumentException("Email da duoc su dung: " + req.email());
        }
        if (userAdminRepository.existsByUsername(req.username())) {
            throw new IllegalArgumentException("Username da duoc su dung: " + req.username());
        }

        String displayName = (req.firstName() + " " + req.lastName()).trim();
        String tempPassword = UUID.randomUUID().toString().substring(0, 8);

        UserEntity user = new UserEntity();
        user.setUsername(req.username());
        user.setEmail(req.email());
        user.setDisplayName(displayName);
        user.setPassword(passwordEncoder.encode(tempPassword));
        user.setRole(RoleEnum.USER);
        user.setStatus(UserStatusEnum.valueOf(req.status().toUpperCase()));

        UserEntity saved = userAdminRepository.save(user);
        sendWelcomeEmail(saved.getEmail(), saved.getUsername(), tempPassword);
        return AdminUserResponse.from(saved);
    }

    public AdminUserResponse updateUser(UUID id, UpdateUserRequest req) {
        UserEntity user = findUserOrThrow(id);

        if (req.displayName() != null) {
            user.setDisplayName(req.displayName());
        }

        if (req.email() != null && !req.email().equals(user.getEmail())) {
            if (userAdminRepository.existsByEmail(req.email())) {
                throw new IllegalArgumentException("Email da duoc su dung: " + req.email());
            }
            user.setEmail(req.email());
        }

        if (req.plan() != null) {
            user.setCurrentPlan(PlanEnum.valueOf(req.plan().toUpperCase()));
        }

        if (req.status() != null) {
            user.setStatus(UserStatusEnum.valueOf(req.status().toUpperCase()));
        }

        if (req.role() != null) {
            requireAdmin();
            user.setRole(RoleEnum.valueOf(req.role().toUpperCase()));
        }

        return AdminUserResponse.from(userAdminRepository.save(user));
    }

    @Transactional
    public Map<String, String> deleteUser(UUID id) {
        userAdminRepository.delete(findUserOrThrow(id));
        return Map.of("message", "Xoa nguoi dung thanh cong");
    }

    public AdminUserResponse lockUser(UUID userId, UserStatusEnum status) {
        UserEntity user = findUserOrThrow(userId);
        user.setStatus(status);
        return AdminUserResponse.from(userAdminRepository.save(user));
    }

    private UserEntity findUserOrThrow(UUID id) {
        UserEntity user = userAdminRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Khong tim thay user: " + id));
        if (user.getRole() != RoleEnum.USER) {
            throw new IllegalArgumentException("Tai khoan khong thuoc nhom user");
        }
        return user;
    }

    private void requireAdmin() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = authentication != null && authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));
        if (!isAdmin) {
            throw new AccessDeniedException("Chi admin moi duoc thay doi vai tro tai khoan");
        }
    }

    private void sendWelcomeEmail(String email, String username, String tempPassword) {
        if (mailSender == null) {
            return;
        }
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(email);
            message.setSubject("[Synkork] Tai khoan cua ban da duoc tao");
            message.setText(String.format(
                    "Xin chao %s,\n\nMat khau tam thoi: %s\n\n"
                            + "Vui long doi mat khau sau khi dang nhap.",
                    username,
                    tempPassword
            ));
            mailSender.send(message);
        } catch (Exception ignored) {
            // Account creation should not fail when email delivery is unavailable.
        }
    }

    public AdminUserResponse lockUser(UUID userId, UserStatusEnum status) {
        UserEntity user = userAdminRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy user!"));
            
        if(user.getStatus() == UserStatusEnum.BANNED){
            throw new RuntimeException("User này đã bị khóa!");
        }

        user.setStatus(status);
        return AdminUserResponse.from(userAdminRepository.save(user));
        
    }
}
