package com.synkork.backend.modules.room;

import com.synkork.backend.common.base.BaseEntity;
import com.synkork.backend.modules.room.enums.RoomStatusEnum;
import com.synkork.backend.modules.room.enums.RoomTypeEnum;
import com.synkork.backend.modules.roomMember.RoomMemberEntity;
import com.synkork.backend.modules.space.SpaceEntity;
import com.synkork.backend.modules.user.UserEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "rooms")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomEntity extends BaseEntity {
    private String name;

    @Column(nullable = true)
    private String avatarUrl;

    @Column(nullable = true)
    private String avatarId;

    @Column(nullable = true)
    private String description;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private RoomTypeEnum type = RoomTypeEnum.GROUP; // GROUP | DM

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private RoomStatusEnum status = RoomStatusEnum.OPEN;

    @Column(unique = true, nullable = true)
    private String inviteCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", columnDefinition = "BINARY(16)", nullable = true)
    private UserEntity owner;

    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RoomMemberEntity> roomMembers;

    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SpaceEntity> spaces;

}
