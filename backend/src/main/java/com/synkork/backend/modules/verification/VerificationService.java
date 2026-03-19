package com.synkork.backend.modules.verification;

import com.synkork.backend.modules.user.UserEntity;
import com.synkork.backend.modules.user.UserRepository;
import com.synkork.backend.modules.user.enums.UserStatusEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class VerificationService {

    @Autowired
    VerificationRepository verificationRepository;

    @Autowired
    UserRepository userRepository;

    public VerificationEntity createVerify(String email, VerifyTypeEnum verifyType) {
        VerificationEntity verificationEntity = VerificationEntity.builder().email(email).type(verifyType).build();
        verificationRepository.save(verificationEntity);
        return verificationEntity;
    }

    public void verify(String token) {
        VerificationEntity entity = verificationRepository.findById(UUID.fromString(token)).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Link không hợp lệ hoặc đã được sử dụng"));;

        if (entity.getExpiredAt().isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.GONE, "Link đã hết hạn"); // 410 Gone
        }

        if (entity.getType() != VerifyTypeEnum.REGISTER) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Link không hợp lệ");
        }

        // Kích hoạt user
        UserEntity user = userRepository.findByEmail(entity.getEmail())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tài khoản không tồn tại"));

        user.setStatus(UserStatusEnum.ACTIVE);
        userRepository.save(user);

        // Xoá token sau khi dùng
        verificationRepository.delete(entity);
    }

    public Optional<VerificationEntity> findById(UUID uuid) {
        return  verificationRepository.findById(uuid);
    }

    public void delete(VerificationEntity verify) {
        verificationRepository.delete(verify);
    }
}
