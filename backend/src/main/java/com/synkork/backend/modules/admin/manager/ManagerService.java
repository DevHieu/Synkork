package com.synkork.backend.modules.admin.manager;

import com.synkork.backend.common.utils.AuthUtils;
import com.synkork.backend.common.utils.EmailService;
import com.synkork.backend.modules.admin.manager.dto.*;
import com.synkork.backend.modules.user.UserEntity;
import com.synkork.backend.modules.user.enums.RoleEnum;
import com.synkork.backend.modules.user.enums.UserStatusEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class ManagerService {

    @Autowired
    private ManagerRepository managerRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    public Page<UserEntity> getManagers(ManagerFilterRequest request) {
        request.validate();

        Specification<UserEntity> spec = ManagerSpecification.filter(request)
                .and((root, query, cb) -> root.get("role").in(RoleEnum.MANAGER, RoleEnum.ADMIN));
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
        String oldDisplayName = account.getDisplayName();
        String oldEmail = account.getEmail();
        UserStatusEnum oldStatus = account.getStatus();
        RoleEnum oldRole = account.getRole();

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
            UserStatusEnum newStatus = parseRequiredStatus(request.getStatus());
            if (isLockedStatus(newStatus)) {
                preventLockingCurrentAccount(id);
            }
            account.setStatus(newStatus);
        }

        if (request.getRole() != null) {
            account.setRole(parseManagedRole(request.getRole()));
        }

        UserEntity saved = managerRepository.save(account);
        if (!isLockedStatus(oldStatus) && isLockedStatus(saved.getStatus())) {
            sendManagerLockedEmail(saved, "Tai khoan cua ban da bi khoa boi quan tri vien.");
        } else {
            sendManagerUpdatedEmail(saved, oldDisplayName, oldEmail, oldStatus, oldRole);
        }
        return ManagerResponse.from(saved);
    }

    @Transactional
    public Map<String, String> lockManager(UUID id, LockManagerRequest request) {
        preventLockingCurrentAccount(id);
        UserEntity account = findManagedAccount(id);
        String reason = Optional.ofNullable(request)
                .map(LockManagerRequest::reason)
                .filter(value -> !value.isBlank())
                .orElse("Tai khoan cua ban da bi khoa boi quan tri vien.");
        account.setStatus(UserStatusEnum.BANNED);
        managerRepository.save(account);
        sendManagerLockedEmail(account, reason);
        return Map.of("message", "Da khoa tai khoan manager/admin thanh cong");
    }

    private void preventLockingCurrentAccount(UUID id) {
        if (id.equals(AuthUtils.getCurrentUserId())) {
            throw new IllegalArgumentException("Khong the tu khoa tai khoan dang dang nhap");
        }
    }

    private boolean isLockedStatus(UserStatusEnum status) {
        return status == UserStatusEnum.INACTIVE || status == UserStatusEnum.BANNED;
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
        String body = plainTextEmailBody(String.format(
                "Xin chao %s,\n\nUsername: %s\nMat khau tam thoi: %s\n\n"
                        + "Vui long doi mat khau sau khi dang nhap.",
                account.getDisplayName(),
                account.getUsername(),
                temporaryPassword
        ));
        emailService.send(account.getEmail(), "[Synkork] Tai khoan quan tri da duoc tao", body);
    }

    private void sendManagerLockedEmail(UserEntity account, String reason) {
        String body = plainTextEmailBody(String.format(
                "Xin chao %s,\n\nTai khoan quan tri Synkork cua ban da bi khoa.\n\n"
                        + "Trang thai hien tai: %s\n"
                        + "Ly do: %s\n\n"
                        + "Neu can ho tro them, vui long lien he quan tri vien.",
                account.getUsername(),
                account.getStatus(),
                reason
        ));
        emailService.send(account.getEmail(), "[Synkork] Tai khoan quan tri cua ban da bi khoa", body);
    }

    private void sendManagerUpdatedEmail(
            UserEntity account,
            String oldDisplayName,
            String oldEmail,
            UserStatusEnum oldStatus,
            RoleEnum oldRole
    ) {
        String body = plainTextEmailBody(String.format(
                "Xin chao %s,\n\nTai khoan quan tri Synkork cua ban da duoc cap nhat.\n\n"
                        + "Thong tin truoc do:\n"
                        + "- Ten hien thi: %s\n"
                        + "- Email: %s\n"
                        + "- Trang thai: %s\n"
                        + "- Vai tro: %s\n\n"
                        + "Thong tin hien tai:\n"
                        + "- Ten hien thi: %s\n"
                        + "- Email: %s\n"
                        + "- Trang thai: %s\n"
                        + "- Vai tro: %s\n\n"
                        + "Neu ban khong yeu cau thay doi nay, vui long lien he quan tri vien.",
                account.getUsername(),
                valueOrDash(oldDisplayName),
                valueOrDash(oldEmail),
                valueOrDash(oldStatus),
                valueOrDash(oldRole),
                valueOrDash(account.getDisplayName()),
                valueOrDash(account.getEmail()),
                valueOrDash(account.getStatus()),
                valueOrDash(account.getRole())
        ));
        emailService.send(account.getEmail(), "[Synkork] Tai khoan quan tri cua ban da duoc cap nhat", body);
    }

    private String valueOrDash(Object value) {
        return value == null ? "-" : value.toString();
    }

    private String plainTextEmailBody(String text) {
        return "<div style=\"font-family: Arial, sans-serif; white-space: pre-line;\">"
                + escapeHtml(text)
                + "</div>";
    }

    private String escapeHtml(String text) {
        if (text == null) {
            return "";
        }
        return text
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
