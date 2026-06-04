package com.synkork.backend.modules.space;

import com.synkork.backend.common.base.BaseEntity;
import com.synkork.backend.modules.message.MessageEntity;
import com.synkork.backend.modules.room.RoomEntity;
import com.synkork.backend.modules.space.enums.SpaceStatusEnum;
import com.synkork.backend.modules.space.enums.SpaceTypeEnum;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name = "spaces")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SpaceEntity extends BaseEntity {
    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    private SpaceTypeEnum type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private RoomEntity room;

    @OneToMany(mappedBy = "space", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MessageEntity> messages;

    @Enumerated(EnumType.STRING)
    private SpaceStatusEnum status =  SpaceStatusEnum.OPEN;

    @Column(nullable = false)
    private boolean isRestricted = false;

    // Khi nào làm chức năng whitelist vào space thì cần cột này, hiện tại chưa cần
//    @Enumerated(EnumType.STRING)
//    @Column(nullable = true)
//    private RestrictTypeEnum restrictType;

    public SpaceEntity(String name, SpaceTypeEnum spaceTypeEnum, RoomEntity roomEntity) {
        this.name = name;
        this.type = spaceTypeEnum;
        this.room = roomEntity;
    }
}