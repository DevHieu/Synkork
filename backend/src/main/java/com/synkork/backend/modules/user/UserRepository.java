package com.synkork.backend.modules.user;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

// Mỗi entity sẽ có một repository tương ứng để thao tác với database
// JpaRepository cung cấp các phương thức CRUD cơ bản
// Mỗi repository nên được đánh dấu với @Repository để Spring có thể quản lý nó như một bean
// Và mỗi repository nên mở rộng JpaRepository với entity tương ứng và kiểu dữ liệu của khóa chính

@Repository
public interface UserRepository extends JpaRepository<UserEntity, UUID> {
  Optional<UserEntity> findByUsername(String username); // Optional to handle user not found case

  Optional<UserEntity> findByEmail(String email);
}
