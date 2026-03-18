package com.synkork.backend.modules.auth;

import com.synkork.backend.common.utils.EmailService;
import com.synkork.backend.modules.auth.dto.LoginRequest;
import com.synkork.backend.modules.auth.dto.RegisterRequest;
import com.synkork.backend.modules.user.UserEntity;
import com.synkork.backend.modules.user.UserRepository;
import com.synkork.backend.modules.user.enums.ProviderEnum;
import com.synkork.backend.modules.user.enums.UserStatusEnum;
import com.synkork.backend.modules.verification.VerificationEntity;
import com.synkork.backend.modules.verification.VerificationService;
import com.synkork.backend.modules.verification.VerifyTypeEnum;
import com.synkork.backend.security.JwtService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

// Xem bên UserService để hiểu thêm (Bố m ghi hết bên đất r đó)
@Service
public class AuthService {

    @Autowired
    UserRepository userRepository;

    @Autowired
    private AuthenticationManager authManager;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private VerificationService verificationService;

    @Autowired
    private EmailService emailService;

    private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

    public String login(LoginRequest request, HttpServletResponse response) {
        UserEntity user = userRepository.findByEmail(request.getUsername())
                .orElseGet(() -> userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Username hoặc Email không tồn tại!")));

        if (user.getProvider() == ProviderEnum.GOOGLE && user.getPassword() == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Tài khoản này đăng nhập bằng Google, vui lòng sử dụng nút 'Đăng nhập với Google'");
        }

        try {
            Authentication authentication = authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            return jwtService.generateJwtToken(user.getId().toString(), userDetails.getUsername(), response);

        } catch (BadCredentialsException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Mật khẩu sai. Vui lòng nhập lại!");
        }
    }

    public void register(RegisterRequest request) {
        Optional<UserEntity> existingByUsername = userRepository.findByUsernameOrEmail(request.getUsername(), request.getUsername());
        if (existingByUsername.isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Tên người dùng đã được sử dụng");
        }

        Optional<UserEntity> existingByEmail = userRepository.findByEmail(request.getEmail());
        if (existingByEmail.isPresent()) {
            UserEntity existing = existingByEmail.get();
            if (existing.getStatus() == UserStatusEnum.INACTIVE) {
                // Chưa verify → gửi lại email verify
                VerificationEntity verify = verificationService.createVerify(request.getEmail(), VerifyTypeEnum.REGISTER);
                emailService.sendVerificationEmail(request.getEmail(), verify.getId().toString());
                return;
            }
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email đã tồn tại");
        }

        UserEntity newUser = UserEntity.builder()
                .displayName(request.getFirstName() + " " + request.getLastName())
                .username(request.getUsername())
                .email(request.getEmail())
                .password(encoder.encode(request.getPassword()))
                .status(UserStatusEnum.INACTIVE)
                .build();

        userRepository.save(newUser);

        VerificationEntity verify = verificationService.createVerify(newUser.getEmail(), VerifyTypeEnum.REGISTER);
        emailService.sendVerificationEmail(newUser.getEmail(), verify.getId().toString());
    }

    public void sendRequestPasswordReset(String email) {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Email không tồn tại"));

        if (user.getStatus() == UserStatusEnum.INACTIVE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tài khoản chưa được xác thực");
        }

        if (user.getProvider() == ProviderEnum.GOOGLE && user.getPassword() == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Tài khoản này đăng nhập bằng Google, vui lòng sử dụng nút 'Đăng nhập với Google'");
        }

        VerificationEntity verify = verificationService.createVerify(email, VerifyTypeEnum.FORGOT_PASSWORD);
        emailService.sendForgotPasswordEmail(email, verify.getId().toString());
    }

    public void resetPassword(String token, String newPassword) {
        VerificationEntity verify = verificationService.findById(UUID.fromString(token))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Link không hợp lệ hoặc đã được sử dụng"));

        if (verify.getExpiredAt().isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.GONE, "Link đã hết hạn");
        }

        if (verify.getType() != VerifyTypeEnum.FORGOT_PASSWORD) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Link không hợp lệ");
        }

        UserEntity user = userRepository.findByEmail(verify.getEmail())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tài khoản không tồn tại"));

        user.setPassword(encoder.encode(newPassword));
        userRepository.save(user);

        verificationService.delete(verify);
    }
}


