package com.synkork.backend.modules.collaboration.note;

import com.synkork.backend.common.base.BaseEntity;
import com.synkork.backend.modules.space.SpaceEntity;
import com.synkork.backend.modules.user.UserEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name="notes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NoteEntity extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "space_id", nullable = false)
    private SpaceEntity space;

    @Column(columnDefinition = "TEXT")
    private String title;

    @Column(columnDefinition = "LONGTEXT")
    private String note;

    private boolean important;
    private boolean allowEditAll;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private UserEntity createdBy;
}
