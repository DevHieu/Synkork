package com.synkork.backend.modules.room;

import com.synkork.backend.common.base.BaseEntity;
import com.synkork.backend.modules.room.enums.RoomTypeEnum;
import com.synkork.backend.modules.user.UserEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name = "rooms")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RoomEntity extends BaseEntity {
    private String name;
    private String roomAvatar;

    @Enumerated(EnumType.STRING)
    private RoomTypeEnum type; // GROUP | DM

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", columnDefinition = "BINARY(16)")
    private UserEntity owner;

    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL)
    private List<RoomMemberEntity> roomMembers;
}
