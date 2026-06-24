package com.synkork.backend.modules.admin.manager;

import com.synkork.backend.modules.admin.manager.dto.*;
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
public class ManagerService {

    @Autowired
    private ManagerRepository managerRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired(required = false)
    private JavaMailSender mailSender;

    public Page<UserEntity> getManagers(ManagerFilterRequest request) {
        request.validate();

        Specification<UserEntity> spec = ManagerSpecification.filter(request)
                .and((root, query, cb) -> cb.equal(root.get("role"), RoleEnum.USER));
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize());

        return managerRepository.findAll(spec, pageable);
    }

    public ManagerResponse getManager(UUID id) {
        return ManagerResponse.from(findManagedAccount(id));
    }

    @Transactional
    public ManagerResponse createManager(CreateManagerRequest request) {
        if (managerRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email da duoc su dung");
        }
        if (managerRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username da duoc su dung");
        }

        String temporaryPassword = UUID.randomUUID().toString().substring(0, 8);
        UserEntity account = new UserEntity();
        account.setDisplayName(request.getDisplayName().trim());
        account.setUsername(request.getUsername().trim());
        account.setEmail(request.getEmail().trim());
        account.setPassword(passwordEncoder.encode(temporaryPassword));
        account.setRole(parseManagedRole(request.getRole()));
        account.setStatus(parseRequiredStatus(request.getStatus()));

        UserEntity saved = managerRepository.save(account);
        sendWelcomeEmail(saved, temporaryPassword);
        return ManagerResponse.from(saved);
    }

    @Transactional
    public ManagerResponse updateManager(UUID id, UpdateManagerRequest request) {
        UserEntity account = findManagedAccount(id);

        if (request.getDisplayName() != null) {
            account.setDisplayName(request.getDisplayName().trim());
        }

        if (request.getEmail() != null && !request.getEmail().equalsIgnoreCase(account.getEmail())) {
            if (managerRepository.existsByEmail(request.getEmail())) {
                throw new IllegalArgumentException("Email da duoc su dung");
            }
            account.setEmail(request.getEmail().trim());
        }

        if (request.getStatus() != null) {
            account.setStatus(parseRequiredStatus(request.getStatus()));
        }

        if (request.getRole() != null) {
            account.setRole(parseManagedRole(request.getRole()));
        }

        return ManagerResponse.from(managerRepository.save(account));
    }

    @Transactional
    public Map<String, String> deleteManager(UUID id) {
        managerRepository.delete(findManagedAccount(id));
        return Map.of("message", "Xoa tai khoan manager thanh cong");
    }

    private UserEntity findManagedAccount(UUID id) {
        UserEntity account = managerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Khong tim thay tai khoan"));
        if (account.getRole() != RoleEnum.MANAGER && account.getRole() != RoleEnum.ADMIN) {
            throw new IllegalArgumentException("Tai khoan khong phai manager hoac admin");
        }
        return account;
    }

    private RoleEnum parseManagedRole(String role) {
        try {
            RoleEnum parsedRole = RoleEnum.valueOf(role.toUpperCase());
            if (parsedRole != RoleEnum.MANAGER && parsedRole != RoleEnum.ADMIN) {
                throw new IllegalArgumentException();
            }
            return parsedRole;
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Vai tro phai la manager hoac admin");
        }
    }

    private RoleEnum parseRole(String role) {
        if (role == null || role.isBlank()) return null;
        return parseManagedRole(role);
    }

    private UserStatusEnum parseStatus(String status) {
        if (status == null || status.isBlank()) return null;
        return parseRequiredStatus(status);
    }

    private UserStatusEnum parseRequiredStatus(String status) {
        try {
            return UserStatusEnum.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Trang thai khong hop le");
        }
    }

    private void sendWelcomeEmail(UserEntity account, String temporaryPassword) {
        if (mailSender == null) return;
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(account.getEmail());
            message.setSubject("[Synkork] Tai khoan quan tri da duoc tao");
            message.setText(String.format(
                    "Xin chao %s,\n\nUsername: %s\nMat khau tam thoi: %s\n\n"
                            + "Vui long doi mat khau sau khi dang nhap.",
                    account.getDisplayName(),
                    account.getUsername(),
                    temporaryPassword
            ));
            mailSender.send(message);
        } catch (Exception ignored) {
            // Account creation should not fail when email delivery is unavailable.
        }
    }
}
