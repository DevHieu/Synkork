package com.synkork.backend.modules.user;

import com.synkork.backend.modules.user.dto.ChangePasswordDto;
import com.synkork.backend.modules.user.dto.UpdateprofileDto;
import com.synkork.backend.modules.user.dto.UserInfoDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

// Service là nơi xử lý các nghiệp vụ chính của ứng dụng
// Nó sẽ tương tác với các repository để lấy dữ liệu từ database và thực hiện các logic nghiệp vụ

// Như Controller thì mỗi Service cũng nên được đánh dấu với @Service để Spring biết đây là một service
@Service
public class UserService {

    // @Autowired sẽ giúp Spring tự động tiêm các bean tương ứng vào, đỡ phải ghi
    // code khởi tạo thủ công
    @Autowired
    UserRepository userRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    public List<UserEntity> findAll() {
        return userRepository.findAll();
    }

    public UserEntity findById(UUID userId) {
        return userRepository.findById(userId).orElse(null);
    }

    // @NonNull annotation giúp đảm bảo rằng user không được null, đỡ bị IDE báo
    public UserEntity create(@NonNull UserEntity user) {
        return userRepository.save(user);
    }

    public UserInfoDto getUserInfo(String username) {
        UserEntity user = userRepository.findByEmail(username)
                .orElseGet(() -> userRepository.findByUsername(username)
                        .orElse(null));

        return new UserInfoDto(user);
    }

    public UserEntity findByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }

    public UserEntity getUserInfoByUsername(String username) {
        return userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("User không tồn tại"));
    }

    public UserEntity updateUser(UserEntity existedUser) {
        return userRepository.save(existedUser);
    }

    // Lấy user hiện tại từ SecurityContext
    public UserEntity getCurrentUser() {
        String email = org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));
    }

    // Cập nhật displayName và username
    public UserInfoDto updateProfile(UpdateprofileDto dto) {
        UserEntity user = getCurrentUser();

        if (dto.displayName() != null && !dto.displayName().isBlank()) {
            user.setDisplayName(dto.displayName().trim());
        }

        if (dto.username() != null && !dto.username().isBlank()) {
            String newUsername = dto.username().trim().toLowerCase();
            userRepository.findByUsername(newUsername).ifPresent(existing -> {
                if (!existing.getId().equals(user.getId())) {
                    throw new RuntimeException("Tên đăng nhập đã được sử dụng");
                }
            });
            user.setUsername(newUsername);
        }

        return new UserInfoDto(userRepository.save(user));
    }

    // Đổi mật khẩu — chỉ dành cho tài khoản LOCAL đã có password
    public void changePassword(ChangePasswordDto dto) {
        UserEntity user = getCurrentUser();

        if (user.getPassword() == null) {
            throw new RuntimeException("Tài khoản chưa có mật khẩu. Hãy dùng chức năng tạo mật khẩu.");
        }

        if (!passwordEncoder.matches(dto.currentPassword(), user.getPassword())) {
            throw new RuntimeException("Mật khẩu hiện tại không đúng");
        }

        if (dto.newPassword() == null || dto.newPassword().length() < 6) {
            throw new RuntimeException("Mật khẩu mới phải có ít nhất 6 ký tự");
        }

        user.setPassword(passwordEncoder.encode(dto.newPassword()));
        userRepository.save(user);
    }

    // Tạo mật khẩu mới — dành cho tài khoản OAuth chưa có password
    public void createPassword(String newPassword) {
        UserEntity user = getCurrentUser();

        if (user.getPassword() != null) {
            throw new RuntimeException("Tài khoản đã có mật khẩu. Hãy dùng chức năng đổi mật khẩu.");
        }

        if (newPassword == null || newPassword.length() < 6) {
            throw new RuntimeException("Mật khẩu phải có ít nhất 6 ký tự");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    public UserInfoDto updateAvatar(String avatarUrl, String avatarId) {
        UserEntity user = getCurrentUser();
        user.setAvatarUrl(avatarUrl);
        user.setAvatarId(avatarId);
        return new UserInfoDto(userRepository.save(user));
    }


}
