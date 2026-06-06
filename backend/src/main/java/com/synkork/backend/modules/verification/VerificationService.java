package com.synkork.backend.modules.verification;

import com.synkork.backend.modules.space.SpaceService;
import com.synkork.backend.modules.user.UserEntity;
import com.synkork.backend.modules.user.UserRepository;
import com.synkork.backend.modules.user.UserService;
import com.synkork.backend.modules.user.enums.UserStatusEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class VerificationService {

    @Autowired
    private VerificationRepository verificationRepository;

    @Autowired
    private UserService userService;
    @Autowired
    private SpaceService spaceService;

    private String generateOTP() {
        return String.format("%06d", new java.security.SecureRandom().nextInt(1000000));
    }

    public VerificationEntity createVerify(String email, VerifyTypeEnum verifyType) {
        UserEntity user = userService.findByEmail(email);

        VerificationEntity verificationEntity = VerificationEntity.builder().user(user).type(verifyType).build();
        verificationRepository.save(verificationEntity);
        return verificationEntity;
    }

    public VerificationEntity createVerifyWithOTP(UserEntity user, VerifyTypeEnum verifyType) {
        String otpCode = generateOTP();
        VerificationEntity verificationEntity = VerificationEntity.builder()
                .user(user)
                .otpCode(otpCode)
                .type(verifyType)
                .build();
        verificationRepository.save(verificationEntity);
        return verificationEntity;
    }

    public void verifyAccountRegister(String token) {
        VerificationEntity entity = verificationRepository.findById(UUID.fromString(token)).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Link không hợp lệ hoặc đã được sử dụng"));;

        if (entity.getExpiredAt().isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.GONE, "Link đã hết hạn"); // 410 Gone
        }

        if (entity.getType() != VerifyTypeEnum.REGISTER) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Link không hợp lệ");
        }

        // Kích hoạt user
        UserEntity user = entity.getUser();

        user.setStatus(UserStatusEnum.ACTIVE);

        Map<String, UUID> personalId = spaceService.createPersonalSpaces(user);

        user.setPersonalNoteId(personalId.get("noteId"));
        user.setPersonalCalendarId(personalId.get("calendarId"));

        userService.create(user);

        // Xoá token sau khi dùng
        verificationRepository.delete(entity);
    }

    public VerificationEntity verifyOtp(String token, String otpCode) {
        VerificationEntity entity = verificationRepository.findById(UUID.fromString(token)).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Link không hợp lệ hoặc đã được sử dụng"));;

        if (!entity.getOtpCode().equals(otpCode)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Sai mã OTP. Mời nhập lại");
        }

        if (entity.getExpiredAt().isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.GONE, "Link đã hết hạn"); // 410 Gone
        }

        return entity;
    }

    public Optional<VerificationEntity> findById(UUID uuid) {
        return  verificationRepository.findById(uuid);
    }

    public void delete(VerificationEntity verify) {
        verificationRepository.delete(verify);
    }

    public void deleteByToken(String token) {
        verificationRepository.findById(UUID.fromString(token))
                .ifPresent(verificationRepository::delete);
    }

    public void deleteByUserAndType(UserEntity user, VerifyTypeEnum type) {
        verificationRepository.findByUserAndType(user, type)
                .ifPresent(verificationRepository::delete);
    }
}
