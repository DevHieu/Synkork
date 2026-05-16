package com.synkork.backend.modules.friend;

import com.synkork.backend.common.utils.uuid.UuidV7Annotation;
import com.synkork.backend.modules.user.UserEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "friends",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"user_id", "friend_id"})
        }
)
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class FriendEntity  {

    @Id
    @UuidV7Annotation
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "friend_id", nullable = false)
    private UserEntity friend;

    private UUID conversationId;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
}
