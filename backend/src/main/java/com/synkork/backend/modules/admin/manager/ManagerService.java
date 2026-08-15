package com.synkork.backend.modules.admin.manager;

import com.synkork.backend.common.utils.AuthUtils;
import com.synkork.backend.modules.admin.auditLog.AuditLogService;
import com.synkork.backend.modules.admin.manager.dto.*;
import com.synkork.backend.modules.admin.manager.email.ManagerEmailService;
import com.synkork.backend.modules.admin.utils.AdminUtils;
import com.synkork.backend.modules.payment.service.ExpiredSubscriptionService;
import com.synkork.backend.modules.payment.service.PaymentService;
import com.synkork.backend.modules.user.UserEntity;
import com.synkork.backend.modules.user.enums.PlanEnum;
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

import java.time.LocalDateTime;
import java.util.List;
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
    private ManagerEmailService managerEmailService;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private ExpiredSubscriptionService expiredSubscriptionService;

    @Autowired
    private AuditLogService auditLogService;

    @Autowired
    private com.fasterxml.jackson.databind.ObjectMapper objectMapper;

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
        String email = request.getEmail().trim();
        if (managerRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email da duoc su dung");
        }

        String username = request.getUsername().trim();
        if (managerRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Username da duoc su dung");
        }

        String temporaryPassword = UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        UserEntity account = new UserEntity();
        account.setDisplayName(request.getDisplayName().trim());
        account.setUsername(username);
        account.setEmail(email);
        account.setPassword(passwordEncoder.encode(temporaryPassword));
        account.setRole(parseManagedRole(request.getRole()));
        account.setStatus(parseRequiredStatus(request.getStatus()));

        UserEntity saved = managerRepository.save(account);

        if (saved.getCurrentPlan() != PlanEnum.FREE) {
            paymentService.createNewSubscription(saved, saved.getCurrentPlan().toString(), null, LocalDateTime.now(), LocalDateTime.now().plusMonths(1));
        }

        managerEmailService.sendManagerAccessEmail(saved, temporaryPassword);
        createLog(saved, com.synkork.backend.modules.admin.auditLog.enums.LogActionEnum.CREATE_MANAGER, null, Map.of(
                "status", saved.getStatus().name(),
                "role", saved.getRole().name(),
                "plan", saved.getCurrentPlan().name()
        ));
        return ManagerResponse.from(saved);
    }

    @Transactional
    public ManagerResponse updateManager(UUID id, UpdateManagerRequest request) {
        UserEntity account = findManagedAccount(id);
        String oldDisplayName = account.getDisplayName();
        String oldEmail = account.getEmail();
        UserStatusEnum oldStatus = account.getStatus();
        RoleEnum oldRole = account.getRole();
        PlanEnum oldPlan = account.getCurrentPlan();

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
            account.setRole(parseAssignableRole(request.getRole()));
        }

        if (request.getPlan() != null) {
            account.setCurrentPlan(parsePlan(request.getPlan()));
        }

        if (request.getPlan() != null) {
            PlanEnum plan = PlanEnum.valueOf(request.getPlan().toUpperCase());

            if (plan != oldPlan) {
                account.setCurrentPlan(plan);

                if (plan != PlanEnum.FREE) {
                    paymentService.createNewSubscription(account, request.getPlan().toUpperCase(), null, LocalDateTime.now(), LocalDateTime.now().plusMonths(1));
                }

                if (AdminUtils.isPlanDowngrade(oldPlan, plan)) {
                    expiredSubscriptionService.pinPendingRemovalRoomAndSpace(List.of(account));
                } else {
                    expiredSubscriptionService.changePendingRoomAndSpace(account.getId());
                }
            }
        }

        UserEntity saved = managerRepository.save(account);
        if (!isLockedStatus(oldStatus) && isLockedStatus(saved.getStatus())) {
            managerEmailService.sendManagerLockedEmail(saved, "Tai khoan cua ban da bi khoa boi quan tri vien.");
        } else {
            managerEmailService.sendManagerUpdatedEmail(saved, oldDisplayName, oldEmail, oldStatus, oldRole);
        }

        createLog(saved, com.synkork.backend.modules.admin.auditLog.enums.LogActionEnum.UPDATE_MANAGER, null, metadata(
                "oldDisplayName", oldDisplayName,
                "newDisplayName", saved.getDisplayName(),
                "oldEmail", oldEmail,
                "newEmail", saved.getEmail(),
                "oldStatus", oldStatus != null ? oldStatus.name() : null,
                "newStatus", saved.getStatus() != null ? saved.getStatus().name() : null,
                "oldRole", oldRole != null ? oldRole.name() : null,
                "newRole", saved.getRole() != null ? saved.getRole().name() : null,
                "oldPlan", oldPlan != null ? oldPlan.name() : null,
                "newPlan", saved.getCurrentPlan() != null ? saved.getCurrentPlan().name() : null
        ));

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
        UserEntity saved = managerRepository.save(account);
        managerEmailService.sendManagerLockedEmail(saved, reason);

        createLog(saved, com.synkork.backend.modules.admin.auditLog.enums.LogActionEnum.LOCK_MANAGER, reason, Map.of(
                "newStatus", UserStatusEnum.BANNED.name()
        ));

        return Map.of("message", "Da khoa tai khoan manager/admin thanh cong");
    }

    private void preventLockingCurrentAccount(UUID id) {
        if (id.equals(AuthUtils.getCurrentUserId())) {
            throw new IllegalArgumentException("Khong the tu khoa tai khoan dang dang nhap");
        }
    }

    private boolean isLockedStatus(UserStatusEnum status) {
        return status == UserStatusEnum.NOT_VERIFIED || status == UserStatusEnum.BANNED;
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

    private RoleEnum parseAssignableRole(String role) {
        try {
            return RoleEnum.valueOf(role.toUpperCase());
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Vai tro phai la user, manager hoac admin");
        }
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

    private PlanEnum parsePlan(String plan) {
        try {
            return PlanEnum.valueOf(plan.toUpperCase());
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Goi dang ky phai la free, team hoac business");
        }
    }

    private Map<String, Object> metadata(Object... keyValues) {
        Map<String, Object> result = new java.util.HashMap<>();
        for (int i = 0; i + 1 < keyValues.length; i += 2) {
            result.put(String.valueOf(keyValues[i]), keyValues[i + 1]);
        }
        return result;
    }

    private void createLog(UserEntity account, com.synkork.backend.modules.admin.auditLog.enums.LogActionEnum action, String reason, Map<String, Object> metadata) {
        com.synkork.backend.modules.admin.auditLog.dtos.BuildLog log = com.synkork.backend.modules.admin.auditLog.dtos.BuildLog.builder()
                .action(action)
                .entityType(com.synkork.backend.modules.admin.auditLog.enums.LogEntityTypeEnum.MANAGER)
                .entityId(account.getId().toString())
                .entityName(account.getEmail())
                .description(AuthUtils.getCurrentUsername() + " performed " + action.name() + " for manager/admin " + account.getEmail())
                .metadata(writeMetadata(metadataWithReason(metadata, reason)))
                .build();

        auditLogService.log(log);
    }

    private Map<String, Object> metadataWithReason(Map<String, Object> metadata, String reason) {
        Map<String, Object> result = new java.util.HashMap<>(metadata);
        if (reason != null && !reason.isBlank()) {
            result.put("reason", reason);
        }
        return result;
    }

    private String writeMetadata(Map<String, Object> metadata) {
        try {
            return objectMapper.writeValueAsString(metadata);
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize audit metadata", e);
        }
    }
}
