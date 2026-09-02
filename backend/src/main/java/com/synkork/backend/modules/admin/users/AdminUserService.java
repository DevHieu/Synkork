package com.synkork.backend.modules.admin.users;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.synkork.backend.common.utils.AuthUtils;
import com.synkork.backend.modules.admin.auditLog.AuditLogService;
import com.synkork.backend.modules.admin.auditLog.dtos.BuildLog;
import com.synkork.backend.modules.admin.auditLog.enums.LogActionEnum;
import com.synkork.backend.modules.admin.auditLog.enums.LogEntityTypeEnum;
import com.synkork.backend.modules.admin.statistics.dtos.UserDashboardChartResponse;
import com.synkork.backend.modules.admin.statistics.dtos.UserStatsResponse;
import com.synkork.backend.modules.admin.users.dtos.AdminUserResponse;
import com.synkork.backend.modules.admin.users.dtos.AdminUserRoomResponse;
import com.synkork.backend.modules.admin.users.dtos.CreateUserRequest;
import com.synkork.backend.modules.admin.users.dtos.DeleteUserRequest;
import com.synkork.backend.modules.admin.users.dtos.UpdateUserRequest;
import com.synkork.backend.modules.admin.users.dtos.UserFilterRequest;
import com.synkork.backend.modules.admin.users.email.AdminUserEmailService;
import com.synkork.backend.modules.admin.utils.AdminUtils;
import com.synkork.backend.modules.collaboration.calendar.repository.CalendarEventRepository;
import com.synkork.backend.modules.payment.service.ExpiredSubscriptionService;
import com.synkork.backend.modules.payment.service.PaymentService;
import com.synkork.backend.modules.report.ReportRepository;
import com.synkork.backend.modules.room.RoomEntity;
import com.synkork.backend.modules.room.RoomRepository;
import com.synkork.backend.modules.roomMember.RoomMemberEntity;
import com.synkork.backend.modules.roomMember.RoomMemberRepository;
import com.synkork.backend.modules.roomMember.RoomMemberService;
import com.synkork.backend.modules.roomMember.enums.MemberStatusEnum;
import com.synkork.backend.modules.roomMember.enums.RoomMemberRoleEnum;
import com.synkork.backend.modules.user.UserEntity;
import com.synkork.backend.modules.user.enums.PlanEnum;
import com.synkork.backend.modules.user.enums.RoleEnum;
import com.synkork.backend.modules.user.enums.UserStatusEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class AdminUserService {

    @Autowired
    private UserAdminRepository userAdminRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RoomMemberRepository roomMemberRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private ReportRepository reportRepository;
    @Autowired
    private CalendarEventRepository calendarEventRepository;

    @Autowired
    private AdminUserEmailService adminUserEmailService;

    @Autowired
    private RoomMemberService roomMemberService;

    @Autowired
    private ExpiredSubscriptionService expiredSubscriptionService;

    @Autowired
    private AuditLogService auditLogService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PaymentService paymentService;

    public UserStatsResponse getUserStatsData(LocalDateTime dateFrom, LocalDateTime dateTo) {
        RoleEnum userRole = RoleEnum.USER;

        LocalDateTime effectiveTo = dateTo != null ? dateTo : LocalDateTime.now();
        LocalDateTime effectiveFrom = dateFrom != null ? dateFrom : effectiveTo.minusMonths(1);

        long totalUsers = userAdminRepository.countByRoleAndCreatedAtLessThanEqual(userRole, effectiveTo);
        double userGrowth = this.calculateUserGrowth(effectiveFrom, effectiveTo, totalUsers);

        LocalDate today = LocalDate.now();
        LocalDateTime startOfToday = today.atStartOfDay();
        LocalDateTime startOfTomorrow = today.plusDays(1).atStartOfDay();
        long newUsersToday = userAdminRepository.countByRoleAndCreatedAtBetween(userRole, startOfToday, startOfTomorrow);

        return new UserStatsResponse(
                totalUsers,
                newUsersToday,
                userGrowth);
    }

    public UserDashboardChartResponse getUserChartData(LocalDateTime dateFrom, LocalDateTime dateTo) {
        RoleEnum userRole = RoleEnum.USER;
        return new UserDashboardChartResponse(
                userAdminRepository.countGroupByStatus(userRole, dateFrom, dateTo),
                userAdminRepository.countGroupByPlan(userRole, dateFrom, dateTo)
        );
    }

    public double calculateUserGrowth(LocalDateTime dateFrom, LocalDateTime dateTo, Long total) {
        long totalUsers = total != null ? total : userAdminRepository.countByRoleAndCreatedAtLessThanEqual(RoleEnum.USER, dateTo);;
        long previousTotalUsers = userAdminRepository.countByRoleAndCreatedAtLessThanEqual(RoleEnum.USER, dateFrom);
        return AdminUtils.calcGrowth(totalUsers, previousTotalUsers);
    }

    private UserEntity findUserById(UUID id) {
        UserEntity user = userAdminRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Khong tim thay user: " + id));
        if (user.getRole() != RoleEnum.USER) {
            throw new IllegalArgumentException("Tai khoan khong thuoc nhom user");
        }
        return user;
    }

    public Page<UserEntity> getUsers(UserFilterRequest request) {
        request.validate();

        Specification<UserEntity> spec = UserSpecification.filter(request)
                .and((root, query, cb) -> cb.equal(root.get("role"), RoleEnum.USER));
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize());

        return userAdminRepository.findAll(spec, pageable);
    }

    public AdminUserResponse getUserById(UUID id) {
        return AdminUserResponse.from(this.findUserById(id));
    }

    public List<AdminUserRoomResponse> getUserRooms(UUID id) {
        this.findUserById(id);

        return roomMemberRepository.findByUserIdWithRoom(id)
                .stream()
                .map(member -> AdminUserRoomResponse.from(
                        member,
                        roomMemberRepository.countByRoom_Id(member.getRoom().getId())
                ))
                .toList();
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
        user.setCurrentPlan(req.plan() != null
                ? PlanEnum.valueOf(req.plan().toUpperCase())
                : PlanEnum.FREE);

        UserEntity saved = userAdminRepository.save(user);

        if (saved.getCurrentPlan() != PlanEnum.FREE) {
            paymentService.createNewSubscription(saved, saved.getCurrentPlan().toString(), null, LocalDateTime.now(), LocalDateTime.now().plusMonths(1));
        }

        adminUserEmailService.sendWelcomeEmail(saved.getEmail(), saved.getUsername(), tempPassword);
        createLog(saved, LogActionEnum.CREATE_USER, null, Map.of(
                "status", saved.getStatus().name(),
                "plan", saved.getCurrentPlan().name()
        ));
        return AdminUserResponse.from(saved);
    }

    @Transactional
    public AdminUserResponse updateUser(UUID id, UpdateUserRequest req) {
        UserEntity user = findUserById(id);
        String oldDisplayName = user.getDisplayName();
        String oldEmail = user.getEmail();
        PlanEnum oldPlan = user.getCurrentPlan();
        UserStatusEnum oldStatus = user.getStatus();
        RoleEnum oldRole = user.getRole();

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
            PlanEnum plan = PlanEnum.valueOf(req.plan().toUpperCase());

            if (plan != oldPlan) {
                user.setCurrentPlan(plan);

                if (plan != PlanEnum.FREE) {
                    paymentService.createNewSubscription(user, req.plan().toUpperCase(), null, LocalDateTime.now(), LocalDateTime.now().plusMonths(1));
                }

                if (AdminUtils.isPlanDowngrade(oldPlan, plan)) {
                    expiredSubscriptionService.pinPendingRemovalRoomAndSpace(List.of(user));
                } else {
                    expiredSubscriptionService.changePendingRoomAndSpace(user.getId());
                }
            }
        }

        if (req.status() != null) {
            UserStatusEnum newStatus = UserStatusEnum.valueOf(req.status().toUpperCase());
            if (newStatus != oldStatus) {
                applyStatusSideEffects(user, newStatus);
                user.setStatus(newStatus);
            }
        }

        if (req.role() != null) {
            requireAdmin();
            user.setRole(RoleEnum.valueOf(req.role().toUpperCase()));
        }

        UserEntity saved = userAdminRepository.save(user);
        adminUserEmailService.sendUserUpdatedEmail(saved, oldDisplayName, oldEmail, oldPlan, oldStatus, oldRole);
        createLog(saved, LogActionEnum.UPDATE_USER, null, metadata(
                "oldDisplayName", oldDisplayName,
                "newDisplayName", saved.getDisplayName(),
                "oldEmail", oldEmail,
                "newEmail", saved.getEmail(),
                "oldPlan", oldPlan != null ? oldPlan.name() : null,
                "newPlan", saved.getCurrentPlan() != null ? saved.getCurrentPlan().name() : null,
                "oldStatus", oldStatus != null ? oldStatus.name() : null,
                "newStatus", saved.getStatus() != null ? saved.getStatus().name() : null,
                "oldRole", oldRole != null ? oldRole.name() : null,
                "newRole", saved.getRole() != null ? saved.getRole().name() : null
        ));
        return AdminUserResponse.from(saved);
    }

    @Transactional
    public Map<String, String> deleteUser(UUID id, DeleteUserRequest request) {
        UserEntity user = findUserById(id);
        String reason = Optional.ofNullable(request)
                .map(DeleteUserRequest::reason)
                .filter(value -> !value.isBlank())
                .orElse("Tai khoan cua ban da bi khoa boi quan tri vien.");

        this.inactiveUserAccount(user);
        user.setStatus(UserStatusEnum.BANNED);
        userAdminRepository.save(user);
        adminUserEmailService.sendUserDeletedEmail(user, reason);
        createLog(user, LogActionEnum.DELETE_USER, reason, Map.of(
                "newStatus", UserStatusEnum.BANNED.name()
        ));

        return Map.of("message", "Da chuyen nguoi dung sang BANNED va xoa khoi cac room dang tham gia");
    }

    @Transactional
    public AdminUserResponse toggleLockUser(UUID userId, String requestedStatus) {
        UserEntity user = findUserById(userId);
        UserStatusEnum oldStatus = user.getStatus();
        UserStatusEnum status = UserStatusEnum.valueOf(requestedStatus.toUpperCase());
        applyStatusSideEffects(user, status);
        user.setStatus(status);
        UserEntity saved = userAdminRepository.save(user);
        if (status == UserStatusEnum.BANNED) {
            adminUserEmailService.sendUserLockedEmail(saved);
        }

        createLog(saved, status == UserStatusEnum.ACTIVE ? LogActionEnum.UNBAN_USER : LogActionEnum.BAN_USER, null, Map.of(
                "oldStatus", oldStatus.name(),
                "newStatus", saved.getStatus().name()
        ));

        return AdminUserResponse.from(saved);
    }

    private void applyStatusSideEffects(UserEntity user, UserStatusEnum newStatus) {
        if (newStatus == UserStatusEnum.ACTIVE) {
            roomMemberRepository.restoreMembersInactiveByAdminLock(user.getId());
            return;
        }

        inactiveUserAccount(user);
    }

    public AdminUserResponse warnUser(UUID userId) {
        UserEntity user = this.findUserById(userId);

        user.setWarning(user.getWarning() + 1);

        UserEntity saved = userAdminRepository.save(user);
        adminUserEmailService.sendUserWarningEmail(saved);
        createLog(saved, LogActionEnum.WARN_USER, null, Map.of(
                "warning", saved.getWarning()
        ));

        return AdminUserResponse.from(saved);
    }

    private void requireAdmin() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = authentication != null && authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));
        if (!isAdmin) {
            throw new AccessDeniedException("Chi admin moi duoc thay doi vai tro tai khoan");
        }
    }

    private void inactiveUserAccount(UserEntity user) {
        roomMemberRepository.deactivateActiveMembersByAdminLock(user.getId());
        roomMemberRepository.updateRoleByUserIdAndInactiveStatus(user.getId(), RoomMemberRoleEnum.MEMBER);

        List<RoomEntity> ownedRooms = roomRepository.findAllByOwnerId(user.getId());
        for (RoomEntity room : ownedRooms) {
            List<RoomMemberEntity> remainingMembers =
                    roomMemberRepository.findByRoom_Id(room.getId())
                            .stream()
                            .filter(member -> !member.getUser().getId().equals(user.getId()))
                            .filter(member -> member.getStatus() == MemberStatusEnum.ACTIVE)
                            .toList();

            if (remainingMembers.isEmpty()) {
                calendarEventRepository.clearCallRoomSpaceByRoomId(room.getId());
                reportRepository.clearTargetRoom(room.getId());
                createRoomLog(room, LogActionEnum.DELETE_ROOM, "Room was deleted because owner was locked and no active members remained", Map.of(
                        "roomId", room.getId().toString(),
                        "roomName", room.getName()
                ));
                roomRepository.delete(room);
            } else {
                roomMemberService.transferOwnerBeforeRemoving(room, remainingMembers);
            }
        }
    }

    private Map<String, Object> metadata(Object... keyValues) {
        Map<String, Object> result = new HashMap<>();
        for (int i = 0; i + 1 < keyValues.length; i += 2) {
            result.put(String.valueOf(keyValues[i]), keyValues[i + 1]);
        }
        return result;
    }

    private void createLog(UserEntity user, LogActionEnum action, String reason, Map<String, Object> metadata) {
        BuildLog log = BuildLog.builder()
                .action(action)
                .entityType(LogEntityTypeEnum.USER)
                .entityId(user.getId().toString())
                .entityName(user.getEmail())
                .description(AuthUtils.getCurrentUsername() + " performed " + action.name() + " for user " + user.getEmail())
                .metadata(writeMetadata(metadataWithReason(metadata, reason)))
                .build();

        auditLogService.log(log);
    }

    private void createRoomLog(RoomEntity room, LogActionEnum action, String reason, Map<String, Object> metadata) {
        BuildLog log = BuildLog.builder()
                .action(action)
                .entityType(LogEntityTypeEnum.ROOM)
                .entityId(room.getId().toString())
                .entityName(room.getName())
                .description(AuthUtils.getCurrentUsername() + " performed " + action.name() + " for room " + room.getName())
                .metadata(writeMetadata(metadataWithReason(metadata, reason)))
                .build();

        auditLogService.log(log);
    }

    private Map<String, Object> metadataWithReason(Map<String, Object> metadata, String reason) {
        Map<String, Object> result = new HashMap<>(metadata);
        if (reason != null && !reason.isBlank()) {
            result.put("reason", reason);
        }
        return result;
    }

    private String writeMetadata(Map<String, Object> metadata) {
        try {
            return objectMapper.writeValueAsString(metadata);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize audit metadata", e);
        }
    }
}
