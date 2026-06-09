package com.synkork.backend.modules.admin.users;

import com.synkork.backend.modules.admin.users.dtos.AdminUserResponse;
import com.synkork.backend.modules.admin.users.dtos.CreateUserRequest;
import com.synkork.backend.modules.admin.users.dtos.UpdateUserRequest;
import com.synkork.backend.modules.admin.users.dtos.UserFilterRequest;
import com.synkork.backend.modules.user.UserEntity;
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

        request.validate(); // validate dateFrom and dateTo

        Specification<UserEntity> spec =
                UserSpecification.filter(request);

        Pageable pageable = PageRequest.of(
                request.getPage(),
                request.getSize()
        );

        return userAdminRepository.findAll(spec, pageable);
    }

    public AdminUserResponse getUserById(UUID id) {
        return AdminUserResponse.from(findOrThrow(id));
    }

    public AdminUserResponse createUser(CreateUserRequest req) {
        if (userAdminRepository.existsByEmail(req.email()))
            throw new IllegalArgumentException("Email đã được sử dụng: " + req.email());
        if (userAdminRepository.existsByUsername(req.username()))
            throw new IllegalArgumentException("Username đã được sử dụng: " + req.username());

        String displayName = (req.firstName() + " " + req.lastName()).trim();
        String tempPassword = UUID.randomUUID().toString().substring(0, 8);

        UserEntity user = new UserEntity();
        user.setUsername(req.username());
        user.setEmail(req.email());
        user.setDisplayName(displayName);
        user.setPassword(passwordEncoder.encode(tempPassword));
        user.setRole(RoleEnum.valueOf(req.role().toUpperCase()));
        user.setStatus(UserStatusEnum.valueOf(req.status().toUpperCase()));

        UserEntity saved = userAdminRepository.save(user);
        sendWelcomeEmail(saved.getEmail(), saved.getUsername(), tempPassword);
        return AdminUserResponse.from(saved);
    }

    public AdminUserResponse updateUser(UUID id, UpdateUserRequest req) {
        UserEntity user = findOrThrow(id);

        if (req.displayName() != null)
            user.setDisplayName(req.displayName());

        if (req.email() != null && !req.email().equals(user.getEmail())) {
            if (userAdminRepository.existsByEmail(req.email()))
                throw new IllegalArgumentException("Email đã được sử dụng: " + req.email());
            user.setEmail(req.email());
        }

        if (req.role() != null)
            user.setRole(RoleEnum.valueOf(req.role().toUpperCase()));

        if (req.status() != null)
            user.setStatus(UserStatusEnum.valueOf(req.status().toUpperCase()));

        return AdminUserResponse.from(userAdminRepository.save(user));
    }

    @Transactional
    public Map<String, String> deleteUser(UUID id) {
        userAdminRepository.delete(findOrThrow(id));
        return Map.of("message", "Xóa người dùng thành công");
    }

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
        } catch (Exception ignored) {
        }
    }
}
