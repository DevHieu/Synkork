package com.synkork.backend.modules.friend;

import com.synkork.backend.common.base.BaseEntity;
import com.synkork.backend.modules.friend.enums.FriendRequestStatus;
import com.synkork.backend.modules.user.UserEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "friend_requests",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"sender_id", "receiver_id"})
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FriendRequestEntity extends BaseEntity {  // ← extends BaseEntity

    // Xóa id, createdAt, updatedAt — đã có trong BaseEntity

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private UserEntity sender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id", nullable = false)
    private UserEntity receiver;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FriendRequestStatus status;

    @Column(columnDefinition = "TEXT")
    private String message;
}