package com.synkork.backend.modules.user;

import java.util.List;
import java.util.Optional;

import com.synkork.backend.modules.user.dto.UserInfoDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

// Service là nơi xử lý các nghiệp vụ chính của ứng dụng
// Nó sẽ tương tác với các repository để lấy dữ liệu từ database và thực hiện các logic nghiệp vụ

// Như Controller thì mỗi Service cũng nên được đánh dấu với @Service để Spring biết đây là một service
@Service
public class UserService {

  // @Autowired sẽ giúp Spring tự động tiêm các bean tương ứng vào, đỡ phải ghi
  // code khởi tạo thủ công
  @Autowired
  UserRepository userRepository;

  public List<UserEntity> findAll() {
    return userRepository.findAll();
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

    public UserEntity updateUser(UserEntity existedUser) {
      return userRepository.save(existedUser);
    }
}
