package com.synkork.backend.modules.space;

import com.synkork.backend.common.base.BaseEntity;
import com.synkork.backend.modules.room.RoomEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
    @JoinColumn(name = "room_id", nullable = false )
    private RoomEntity room;
}