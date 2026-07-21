package com.synkork.backend.modules.space;

import com.synkork.backend.common.base.BaseEntity;
import com.synkork.backend.modules.collaboration.calendar.entity.CalendarEventEntity;
import com.synkork.backend.modules.collaboration.note.NoteEntity;
import com.synkork.backend.modules.collaboration.task.column.ColumnEntity;
import com.synkork.backend.modules.message.MessageEntity;
import com.synkork.backend.modules.room.RoomEntity;
import com.synkork.backend.modules.space.enums.SpaceStatusEnum;
import com.synkork.backend.modules.space.enums.SpaceTypeEnum;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "spaces")
@Getter
@Setter
@Builder
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

    @OneToMany(mappedBy = "space", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<NoteEntity> notes;

    @OneToMany(mappedBy = "space", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ColumnEntity> columns;

    @OneToMany(mappedBy = "space", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CalendarEventEntity> calendarEvents;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private SpaceStatusEnum status =  SpaceStatusEnum.OPEN;

    @Column(nullable = false)
    @Builder.Default
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
