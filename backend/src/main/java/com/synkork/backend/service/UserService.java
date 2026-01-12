package com.synkork.backend.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.synkork.backend.entity.User;
import com.synkork.backend.repository.UserRepository;

// Service là nơi xử lý các nghiệp vụ chính của ứng dụng
// Nó sẽ tương tác với các repository để lấy dữ liệu từ database và thực hiện các logic nghiệp vụ

// Như Controller thì mỗi Service cũng nên được đánh dấu với @Service để Spring biết đây là một service
@Service
public class UserService {

  // @Autowired sẽ giúp Spring tự động tiêm các bean tương ứng vào, đỡ phải ghi
  // code khởi tạo thủ công
  @Autowired
  UserRepository userRepository;

  public List<User> findAll() {
    return userRepository.findAll();
  }

  public User create(User user) {
    return userRepository.save(user);
  }
}
