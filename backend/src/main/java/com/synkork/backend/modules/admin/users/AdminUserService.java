package com.synkork.backend.modules.admin.users;

import com.synkork.backend.common.utils.EmailService;
import com.synkork.backend.modules.admin.users.dtos.AdminUserResponse;
import com.synkork.backend.modules.admin.users.dtos.CreateUserRequest;
import com.synkork.backend.modules.admin.users.dtos.DeleteUserRequest;
import com.synkork.backend.modules.admin.users.dtos.UpdateUserRequest;
import com.synkork.backend.modules.admin.users.dtos.UserFilterRequest;
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
import jakarta.persistence.EntityManager;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
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
    private EntityManager entityManager;

    @Autowired
    private EmailService emailService;
    @Autowired
    private RoomMemberService roomMemberService;

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
            user.setCurrentPlan(PlanEnum.valueOf(req.plan().toUpperCase()));
        }

        if (req.status() != null) {
            user.setStatus(UserStatusEnum.valueOf(req.status().toUpperCase()));
        }

        if (req.role() != null) {
            requireAdmin();
            user.setRole(RoleEnum.valueOf(req.role().toUpperCase()));
        }

        UserEntity saved = userAdminRepository.save(user);
        sendUserUpdatedEmail(saved, oldDisplayName, oldEmail, oldPlan, oldStatus, oldRole);
        return AdminUserResponse.from(saved);
    }

    @Transactional
    public Map<String, String> deleteUser(UUID id, DeleteUserRequest request) {
        UserEntity user = findUserOrThrow(id);
        String reason = Optional.ofNullable(request)
                .map(DeleteUserRequest::reason)
                .filter(value -> !value.isBlank())
                .orElse("Tai khoan cua ban da bi khoa boi quan tri vien.");

        this.inactiveUserAccount(user);
        user.setStatus(UserStatusEnum.INACTIVE);
        userAdminRepository.save(user);
        sendUserDeletedEmail(user, reason);

        return Map.of("message", "Da chuyen nguoi dung sang INACTIVE va xoa khoi cac room dang tham gia");
    }

    public AdminUserResponse toggleLockUser(UUID userId, UserStatusEnum status) {
        UserEntity user = findUserOrThrow(userId);
        user.setStatus(status);
        UserEntity saved = userAdminRepository.save(user);

        if (status == UserStatusEnum.BANNED) {
            this.inactiveUserAccount(user);

            String targetName = saved.getDisplayName() != null && !saved.getDisplayName().isBlank()
                    ? saved.getDisplayName()
                    : saved.getUsername();

            emailService.sendLockEmail(saved.getEmail(), targetName, "tài khoản của bạn");
        } else if (status == UserStatusEnum.ACTIVE) {
            roomMemberRepository.updateStatusByUserId(user.getId(), MemberStatusEnum.ACTIVE);
        }

        return AdminUserResponse.from(saved);
    }

    public AdminUserResponse warnUser(UUID userId) {
        UserEntity user = findUserOrThrow(userId);

        user.setWarning(user.getWarning() + 1);

        UserEntity saved = userAdminRepository.save(user);
        String targetName = saved.getDisplayName() != null && !saved.getDisplayName().isBlank()
                ? saved.getDisplayName()
                : saved.getUsername();
        emailService.sendWarningEmail(saved.getEmail(), targetName, "tài khoản của bạn", saved.getWarning());

        return AdminUserResponse.from(saved);
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
        String body = plainTextEmailBody(String.format(
                "Xin chao %s,\n\nMat khau tam thoi: %s\n\n"
                        + "Vui long doi mat khau sau khi dang nhap.",
                username,
                tempPassword
        ));
        emailService.send(email, "[Synkork] Tai khoan cua ban da duoc tao", body);
    }

    private void inactiveUserAccount(UserEntity user) {
        roomMemberRepository.updateStatusByUserId(user.getId(), MemberStatusEnum.INACTIVE);
        roomMemberRepository.updateRoleByUserId(user.getId(), RoomMemberRoleEnum.MEMBER);

        List<RoomEntity> ownedRooms = roomRepository.findAllByOwnerId(user.getId());
        for (RoomEntity room : ownedRooms) {
            List<RoomMemberEntity> remainingMembers =
                    roomMemberRepository.findByRoom_Id(room.getId())
                            .stream()
                            .filter(member -> !member.getUser().getId().equals(user.getId()))
                            .toList();

            roomMemberService.transferOwnerBeforeRemoving(room, remainingMembers);
        }
    }

    private void sendUserUpdatedEmail(
            UserEntity user,
            String oldDisplayName,
            String oldEmail,
            PlanEnum oldPlan,
            UserStatusEnum oldStatus,
            RoleEnum oldRole
    ) {
        String body = plainTextEmailBody(String.format(
                "Xin chao %s,\n\nTai khoan Synkork cua ban da duoc cap nhat.\n\n"
                        + "Thong tin truoc do:\n"
                        + "- Ten hien thi: %s\n"
                        + "- Email: %s\n"
                        + "- Goi: %s\n"
                        + "- Trang thai: %s\n"
                        + "- Vai tro: %s\n\n"
                        + "Thong tin hien tai:\n"
                        + "- Ten hien thi: %s\n"
                        + "- Email: %s\n"
                        + "- Goi: %s\n"
                        + "- Trang thai: %s\n"
                        + "- Vai tro: %s\n\n"
                        + "Neu ban khong yeu cau thay doi nay, vui long lien he quan tri vien.",
                user.getUsername(),
                valueOrDash(oldDisplayName),
                valueOrDash(oldEmail),
                valueOrDash(oldPlan),
                valueOrDash(oldStatus),
                valueOrDash(oldRole),
                valueOrDash(user.getDisplayName()),
                valueOrDash(user.getEmail()),
                valueOrDash(user.getCurrentPlan()),
                valueOrDash(user.getStatus()),
                valueOrDash(user.getRole())
        ));
        emailService.send(user.getEmail(), "[Synkork] Tai khoan cua ban da duoc cap nhat", body);
    }

    private void sendUserDeletedEmail(UserEntity user, String reason) {
        String body = plainTextEmailBody(String.format(
                "Xin chao %s,\n\nTai khoan Synkork cua ban da duoc chuyen sang trang thai INACTIVE.\n\n"
                        + "Ly do: %s\n\n"
                        + "Ban da duoc xoa khoi tat ca room dang tham gia. "
                        + "Neu can ho tro them, vui long lien he quan tri vien.",
                user.getUsername(),
                reason
        ));
        emailService.send(user.getEmail(), "[Synkork] Tai khoan cua ban da bi khoa", body);
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

    // public AdminUserResponse lockUser(UUID userId, UserStatusEnum status) {
    //     UserEntity user = userAdminRepository.findById(userId)
    //         .orElseThrow(() -> new RuntimeException("Không tìm thấy user!"));
            
    //     if(user.getStatus() == UserStatusEnum.BANNED){
    //         throw new RuntimeException("User này đã bị khóa!");
    //     }

    //     user.setStatus(status);
    //     return AdminUserResponse.from(userAdminRepository.save(user));
        
    // }
}
