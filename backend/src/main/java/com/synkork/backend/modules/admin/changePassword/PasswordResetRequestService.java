package com.synkork.backend.modules.admin.changePassword;

import com.synkork.backend.common.utils.EmailService;
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
import org.springframework.http.HttpStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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
    private EmailService emailService;

    @Autowired
    private PasswordResetRequestEmailService passwordResetRequestEmailService;

    @Autowired
    private VerificationService verificationService;

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

        emailService.sendOTPEmail(entity.getUser().getEmail(), entity.getOtpCode());
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
        passwordResetRequestRepository.save(request);

        // Gửi mail thông báo
        passwordResetRequestEmailService.sendApprovedEmail(request.getUser().getEmail());
    }

    public void reject(UUID id) {
        PasswordResetRequestEntity request = passwordResetRequestRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy request"));

        if (request.getStatus() != PasswordResetStatusEnum.PENDING) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Request đã được xử lý");
        }

        request.setStatus(PasswordResetStatusEnum.REJECTED);
        passwordResetRequestRepository.save(request);

        passwordResetRequestEmailService.sendRejectedEmail(request.getUser().getEmail());
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
