package com.synkork.backend.modules.auth;

import com.synkork.backend.common.utils.EmailService;
import com.synkork.backend.modules.auth.dto.LoginRequest;
import com.synkork.backend.modules.auth.dto.PasswordResetVerifyRequest;
import com.synkork.backend.modules.auth.dto.RegisterRequest;
import com.synkork.backend.modules.user.UserEntity;
import com.synkork.backend.modules.user.UserRepository;
import com.synkork.backend.modules.user.UserService;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

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
    private AuthEmail authEmail;

    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private UserService userService;


    public String login(LoginRequest request, HttpServletResponse response) {
        UserEntity user = userRepository.findByEmail(request.getUsername())
                .orElseGet(() -> userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Username hoặc Email không tồn tại!")));

        if (user.getProvider() == ProviderEnum.GOOGLE && user.getPassword() == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Tài khoản này đăng nhập bằng Google, vui lòng sử dụng nút 'Đăng nhập với Google'");
        }

        if (user.getStatus() == UserStatusEnum.NOT_VERIFIED) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Tài khoản này chưa xác minh qua email. Vui lòng kiểm tra email của bạn để xác minh");
        }

        if (user.getStatus() == UserStatusEnum.BANNED) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Tài khoản của bạn đã bị khóa do quản trị viên. Vui lòng liên hệ để được giải quyết");
        }

        try {
            Authentication authentication = authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            return jwtService.generateJwtToken(user.getId().toString(), userDetails.getUsername(), user.getRole(), response);

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
            if (existing.getStatus() == UserStatusEnum.NOT_VERIFIED) {
                // Chưa verify → gửi lại email verify
                VerificationEntity verify = verificationService.createVerify(request.getEmail(), VerifyTypeEnum.REGISTER);
                authEmail.sendVerificationEmail(request.getEmail(), verify.getId().toString());
                return;
            }
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email đã tồn tại");
        }

        UserEntity newUser = UserEntity.builder()
                .displayName(request.getFirstName() + " " + request.getLastName())
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .status(UserStatusEnum.NOT_VERIFIED)
                .build();

        userRepository.save(newUser);

        VerificationEntity verify = verificationService.createVerify(newUser.getEmail(), VerifyTypeEnum.REGISTER);
        authEmail.sendVerificationEmail(newUser.getEmail(), verify.getId().toString());
    }

    // return id để đưa cho frontend còn gửi lại lúc nhập OTP xong
    public void sendRequestPasswordReset(String email) {
        UserEntity user = userService.findByEmail(email);

        if (user.getStatus() == UserStatusEnum.NOT_VERIFIED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tài khoản này chưa xác minh qua email. Vui lòng kiểm tra email của bạn để xác minh");
        }

        if (user.getProvider() == ProviderEnum.GOOGLE && user.getPassword() == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Tài khoản này đăng nhập bằng Google, vui lòng sử dụng nút 'Đăng nhập với Google'");
        }

        verificationService.deleteByUserAndType(user, VerifyTypeEnum.FORGOT_PASSWORD);

        VerificationEntity entity = verificationService.createVerifyWithOTP(user, VerifyTypeEnum.FORGOT_PASSWORD);
        authEmail.sendOTPEmail(entity.getUser().getEmail(), entity.getOtpCode());
    }

    public void resetPassword(PasswordResetVerifyRequest request) {
        VerificationEntity verify = verificationService.verifyOtp(request.email(), request.otpCode());

        UserEntity user = verify.getUser();

        user.setPassword(passwordEncoder.encode(request.password()));
        userRepository.save(user);
    }
}


