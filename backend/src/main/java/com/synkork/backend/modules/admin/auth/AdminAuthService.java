
package com.synkork.backend.modules.admin.auth;

import com.synkork.backend.common.utils.AuthUtils;
import com.synkork.backend.modules.admin.auth.dto.AdminChangePasswordRequest;
import com.synkork.backend.modules.admin.auth.dto.AdminUpdateProfileRequest;
import com.synkork.backend.modules.auth.dto.LoginRequest;
import com.synkork.backend.modules.user.UserEntity;
import com.synkork.backend.modules.user.UserRepository;
import com.synkork.backend.modules.user.dto.UserInfoDto;
import com.synkork.backend.modules.user.enums.RoleEnum;
import com.synkork.backend.modules.user.enums.UserStatusEnum;
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

@Service
public class AdminAuthService {

    @Autowired
    UserRepository userRepository;

    @Autowired
    private AuthenticationManager authManager;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private UserEntity getCurrentUser() {
        String email = AuthUtils.getCurrentUsername();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy người dùng"));
    }

    public boolean validateAccount(UserEntity user) {
        if (user.getRole() == RoleEnum.USER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Tài khoản của bạn không phải là tài khoản quản trị viên");
        }

        if (user.getStatus() == UserStatusEnum.BANNED) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Tài khoản của bạn đã bị khóa do quản trị viên. Vui lòng liên hệ để được giải quyết");
        }

        if (user.getStatus() == UserStatusEnum.NOT_VERIFIED) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Tài khoản này chưa xác minh qua email. Vui lòng kiểm tra email của bạn để xác minh");
        }

        return true;
    }

    public String login(LoginRequest request, HttpServletResponse response) {
        UserEntity user = userRepository.findByEmail(request.getUsername())
                .orElseGet(() -> userRepository.findByUsername(request.getUsername())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Username hoặc Email không tồn tại!")));

        if (!validateAccount(user)) {
            return null;
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

    public UserInfoDto updateProfile(AdminUpdateProfileRequest dto) {
        UserEntity user = getCurrentUser();

        String newUsername = dto.username().trim().toLowerCase();
        userRepository.findByUsername(newUsername).ifPresent(existing -> {
            if (!existing.getId().equals(user.getId())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Tên đăng nhập đã được sử dụng");
            }
        });

        user.setUsername(newUsername);
        user.setDisplayName(dto.displayName().trim());

        return new UserInfoDto(userRepository.save(user));
    }

    public void changePassword(AdminChangePasswordRequest dto) {
        UserEntity user = getCurrentUser();

        if (user.getPassword() == null || user.getPassword().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tài khoản chưa có mật khẩu. Hãy dùng chức năng tạo mật khẩu.");
        }

        if (!passwordEncoder.matches(dto.currentPassword(), user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mật khẩu hiện tại không đúng");
        }

        user.setPassword(passwordEncoder.encode(dto.newPassword()));
        userRepository.save(user);
    }
}
