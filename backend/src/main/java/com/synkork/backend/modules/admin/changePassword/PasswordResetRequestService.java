package com.synkork.backend.modules.admin.changePassword;

import com.synkork.backend.common.utils.EmailService;
import com.synkork.backend.modules.admin.changePassword.enums.PasswordResetStatusEnum;
import com.synkork.backend.modules.user.UserEntity;
import com.synkork.backend.modules.user.UserService;
import com.synkork.backend.modules.user.enums.RoleEnum;
import com.synkork.backend.modules.verification.VerificationEntity;
import com.synkork.backend.modules.verification.VerificationService;
import com.synkork.backend.modules.verification.VerifyTypeEnum;
import org.springframework.beans.factory.annotation.Autowired;
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
    private EmailService emailService;

    @Autowired
    private VerificationService verificationService;

        public String createRequest(String email, String newPassword) {
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
            // Lưu tạm request đổi mật khẩu

            PasswordResetRequestEntity request = PasswordResetRequestEntity.builder()
                    .user(user)
                    .newPassword(newPassword)
                    .status(PasswordResetStatusEnum.NOT_VERIFIED)
                    .build();
            passwordResetRequestRepository.save(request);
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
        emailService.sendPasswordResetApprovedEmail(request.getUser().getEmail());
    }

    public void reject(UUID id) {
        PasswordResetRequestEntity request = passwordResetRequestRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy request"));

        request.setStatus(PasswordResetStatusEnum.REJECTED);
        passwordResetRequestRepository.save(request);
    }

    public void changeStatusByEmail(String email, PasswordResetStatusEnum status) {
        PasswordResetRequestEntity entity = passwordResetRequestRepository.findByUserEmail(email).orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Không có yêu cầu đổi mật khẩu của email này"
        ));

        entity.setStatus(status);
        passwordResetRequestRepository.save(entity);
    }
}