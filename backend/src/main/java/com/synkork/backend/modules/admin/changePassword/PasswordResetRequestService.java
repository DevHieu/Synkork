package com.synkork.backend.modules.admin.changePassword;

import com.synkork.backend.common.utils.AuthUtils;
import com.synkork.backend.modules.admin.auditLog.AuditLogService;
import com.synkork.backend.modules.admin.auditLog.dtos.BuildLog;
import com.synkork.backend.modules.admin.auditLog.enums.LogActionEnum;
import com.synkork.backend.modules.admin.auditLog.enums.LogEntityTypeEnum;
import com.synkork.backend.modules.admin.changePassword.dto.PasswordResetRequestFilter;
import com.synkork.backend.modules.admin.changePassword.email.PasswordResetRequestEmailService;
import com.synkork.backend.modules.admin.changePassword.enums.PasswordResetStatusEnum;
import com.synkork.backend.modules.user.UserEntity;
import com.synkork.backend.modules.user.UserService;
import com.synkork.backend.modules.user.enums.RoleEnum;
import com.synkork.backend.modules.verification.VerificationEntity;
import com.synkork.backend.modules.verification.VerificationService;
import com.synkork.backend.modules.verification.VerifyTypeEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
public class PasswordResetRequestService {

    @Autowired
    private PasswordResetRequestRepository passwordResetRequestRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private PasswordResetRequestEmailService passwordResetRequestEmailService;

    @Autowired
    private VerificationService verificationService;

    @Autowired
    private AuditLogService auditLogService;

    public Page<PasswordResetRequestEntity> getRequests(PasswordResetRequestFilter filter) {
        filter.validate();
        PageRequest pageable = PageRequest.of(
                filter.getPage(),
                filter.getSize(),
                Sort.by(Sort.Direction.DESC, "createdAt")
        );
        return passwordResetRequestRepository.findAll(
                PasswordResetRequestSpecification.filter(filter),
                pageable
        );
    }

    public String createRequest(String email) {
        UserEntity user = userService.findByEmail(email);

        if (user.getRole() == RoleEnum.USER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Tài khoản của bạn không phải là tài khoản quản trị viên");
        }

        // Xoá verification cũ nếu có
        verificationService.deleteByUserAndType(user, VerifyTypeEnum.FORGOT_PASSWORD);

        // Xoá password reset request cũ nếu có
        passwordResetRequestRepository.findByUserEmail(email)
                .ifPresent(passwordResetRequestRepository::delete);

        // Tạo ra otp code verify
        VerificationEntity entity = verificationService.createVerifyWithOTP(user, VerifyTypeEnum.FORGOT_PASSWORD);

        passwordResetRequestEmailService.sendOtpEmail(entity.getUser().getEmail(), entity.getOtpCode());
        return entity.getId().toString();
    }

    public void approve(UUID id) {
        PasswordResetRequestEntity request = passwordResetRequestRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy request"));

        if (request.getStatus() != PasswordResetStatusEnum.PENDING) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Request đã được xử lý");
        }

        // Đổi password
        UserEntity user = userService.findByEmail(request.getUser().getEmail());

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userService.create(user);

        request.setStatus(PasswordResetStatusEnum.APPROVED);
        PasswordResetRequestEntity saved = passwordResetRequestRepository.save(request);

        // Gửi mail thông báo
        passwordResetRequestEmailService.sendApprovedEmail(request.getUser().getEmail());

        auditLogService.log(BuildLog.builder()
                .action(LogActionEnum.APPROVE_PASSWORD_RESET)
                .entityType(LogEntityTypeEnum.PASSWORD_RESET_REQUEST)
                .entityId(saved.getId().toString())
                .entityName(saved.getUser().getEmail())
                .description(AuthUtils.getCurrentUsername() + " đã duyệt yêu cầu đổi mật khẩu của " + saved.getUser().getEmail())
                .build());
    }

    public void reject(UUID id) {
        PasswordResetRequestEntity request = passwordResetRequestRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy request"));

        if (request.getStatus() != PasswordResetStatusEnum.PENDING) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Request đã được xử lý");
        }

        request.setStatus(PasswordResetStatusEnum.REJECTED);
        PasswordResetRequestEntity saved = passwordResetRequestRepository.save(request);

        passwordResetRequestEmailService.sendRejectedEmail(request.getUser().getEmail());

        auditLogService.log(BuildLog.builder()
                .action(LogActionEnum.REJECT_PASSWORD_RESET)
                .entityType(LogEntityTypeEnum.PASSWORD_RESET_REQUEST)
                .entityId(saved.getId().toString())
                .entityName(saved.getUser().getEmail())
                .description(AuthUtils.getCurrentUsername() + " đã từ chối yêu cầu đổi mật khẩu của " + saved.getUser().getEmail())
                .build());
    }

    public void buildChangePasswordRequest(UserEntity requester, String newPassword) {

        PasswordResetRequestEntity request = PasswordResetRequestEntity.builder()
                .user(requester)
                .newPassword(newPassword)
                .status(PasswordResetStatusEnum.PENDING)
                .build();
        passwordResetRequestRepository.save(request);
    }
}
