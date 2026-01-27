package com.synkork.backend.common.base;

import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

// BaseEntity sẽ là lớp cha cho tất cả các entity khác
// Chứa các thuộc tính chung như id, createdAt, updatedAt
// Sử dụng @MappedSuperclass để các entity kế thừa có thể sử dụng các thuộc tính này

@MappedSuperclass
@Getter
@Setter
public abstract class BaseEntity {

    @Id
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    @Column(name = "id", columnDefinition = "BINARY(16)")
    private UUID id;

  @CreationTimestamp
  @Column(updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  private LocalDateTime updatedAt;
}
