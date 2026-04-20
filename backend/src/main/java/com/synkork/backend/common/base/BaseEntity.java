package com.synkork.backend.common.base;

import java.time.LocalDateTime;
import java.util.UUID;

import com.synkork.backend.common.utils.uuid.UuidV7Annotation;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;

// BaseEntity sẽ là lớp cha cho tất cả các entity khác
// Chứa các thuộc tính chung như id, createdAt, updatedAt
// Sử dụng @MappedSuperclass để các entity kế thừa có thể sử dụng các thuộc tính này

@MappedSuperclass
@Getter
@Setter
public abstract class BaseEntity {

    @Id
    @UuidV7Annotation
    private UUID id;

  @CreationTimestamp
  @Column(updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  private LocalDateTime updatedAt;
}
