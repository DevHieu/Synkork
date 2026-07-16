package com.synkork.backend.modules.admin.users;

import com.synkork.backend.modules.admin.users.dtos.AdminUserResponse;
import com.synkork.backend.modules.admin.users.dtos.CreateUserRequest;
import com.synkork.backend.modules.admin.users.dtos.DeleteUserRequest;
import com.synkork.backend.modules.admin.users.dtos.UpdateUserRequest;
import com.synkork.backend.modules.admin.users.dtos.UserFilterRequest;
import com.synkork.backend.modules.admin.users.email.AdminUserEmailService;
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
    private AdminUserEmailService adminUserEmailService;
    @Autowired
    private RoomMemberService roomMemberService;

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
        adminUserEmailService.sendWelcomeEmail(saved.getEmail(), saved.getUsername(), tempPassword);
        return AdminUserResponse.from(saved);
    }

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
        adminUserEmailService.sendUserUpdatedEmail(saved, oldDisplayName, oldEmail, oldPlan, oldStatus, oldRole);
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
        user.setStatus(UserStatusEnum.INACTIVE);
        userAdminRepository.save(user);
        adminUserEmailService.sendUserDeletedEmail(user, reason);

        return Map.of("message", "Da chuyen nguoi dung sang INACTIVE va xoa khoi cac room dang tham gia");
    }

    public AdminUserResponse toggleLockUser(UUID userId, UserStatusEnum status) {
        UserEntity user = findUserById(userId);
        user.setStatus(status);
        UserEntity saved = userAdminRepository.save(user);

        if (status == UserStatusEnum.BANNED) {
            this.inactiveUserAccount(user);

            adminUserEmailService.sendUserLockedEmail(saved);
        } else if (status == UserStatusEnum.ACTIVE) {
            roomMemberRepository.updateStatusByUserId(user.getId(), MemberStatusEnum.ACTIVE);
        }

        return AdminUserResponse.from(saved);
    }

    public AdminUserResponse warnUser(UUID userId) {
        UserEntity user = this.findUserById(userId);

        user.setWarning(user.getWarning() + 1);

        UserEntity saved = userAdminRepository.save(user);
        adminUserEmailService.sendUserWarningEmail(saved);

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

