package com.synkork.backend.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.synkork.backend.entity.User;

// Mỗi entity sẽ có một repository tương ứng để thao tác với database
// JpaRepository cung cấp các phương thức CRUD cơ bản
// Mỗi repository nên được đánh dấu với @Repository để Spring có thể quản lý nó như một bean
// Và mỗi repository nên mở rộng JpaRepository với entity tương ứng và kiểu dữ liệu của khóa chính

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
  Optional<User> findByUsername(String username); // Optional to handle user not found case
}
