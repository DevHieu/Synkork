package com.synkork.backend.modules.statistics;

import com.synkork.backend.common.utils.uuid.UuidV7Annotation;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "statistics")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StatisticsEntity {

    @Id
    @UuidV7Annotation
    private UUID id;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private long newUsers;

    @Column(nullable = false)
    private long newRooms;

    @Column(nullable = false)
    private long newSubscriptions;

    @Column(nullable = false)
    private long userOnlines;

    @Column(nullable = false)
    private long totalUsers;

    @Column(nullable = false)
    private long totalRooms;

    @Column(nullable = false)
    private long totalSubscriptions;
}
