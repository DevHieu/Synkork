package com.synkork.backend.modules.roomMember;

import com.synkork.backend.common.utils.uuid.UuidV7Annotation;
import com.synkork.backend.modules.message.MessageEntity;
import com.synkork.backend.modules.room.RoomEntity;
import com.synkork.backend.modules.roomMember.enums.RoomMemberRoleEnum;
import com.synkork.backend.modules.user.UserEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(
        name = "room_members",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"room_id", "user_id"})
        }
)
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomMemberEntity {

    @Id
    @UuidV7Annotation
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private RoomEntity room;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @OneToMany(mappedBy = "sender", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MessageEntity> messages;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private RoomMemberRoleEnum role = RoomMemberRoleEnum.MEMBER;

    @Builder.Default
    private LocalDateTime joinedAt =  LocalDateTime.now();

    @Builder.Default
    private boolean muted = false;

    @Builder.Default
    private boolean deafen = false;

    @Column(nullable = true)
    private LocalDateTime chatDisableUntil;

}
