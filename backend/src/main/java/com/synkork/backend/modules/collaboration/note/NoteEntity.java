package com.synkork.backend.modules.collaboration.note;

import com.synkork.backend.common.base.BaseEntity;
import com.synkork.backend.modules.space.SpaceEntity;
import com.synkork.backend.modules.user.UserEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Builder;

import java.time.Instant;

@Entity
@Table(
        name = "notes",
        indexes = {
                @Index(name = "idx_notes_space_id", columnList = "space_id"),
                @Index(name = "idx_notes_created_by", columnList = "created_by")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NoteEntity extends BaseEntity {

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "space_id", nullable = false, columnDefinition = "BINARY(16)")
    private SpaceEntity space;

    @Column(columnDefinition = "TEXT")
    private String title;

    @Column(columnDefinition = "LONGTEXT")
    private String note;

    @Builder.Default
    private Boolean pinned = false;

    @Builder.Default
    private boolean allowEditAll = true;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "created_by", nullable = false, columnDefinition = "BINARY(16)")
    private UserEntity createdBy;

    @Column(name = "color", length = 7)
    private String color;

    @Column(name = "pos_x", columnDefinition = "INT DEFAULT 0")
    private Integer posX = 0;

    @Column(name = "pos_y", columnDefinition = "INT DEFAULT 0")
    private Integer posY = 0;

    @Column(name = "width", columnDefinition = "INT DEFAULT 2")
    private Integer width = 2;

    @Column(name = "height", columnDefinition = "INT DEFAULT 2")
    private Integer height = 2;

    @Column(name = "reminder_at")
    private Instant reminderAt;

    @Builder.Default
    @Column(name = "reminder_sent")
    private Boolean reminderSent = false;

    @Builder.Default
    @Column(name = "archived")
    private Boolean archived = false;

    @Version
    private Integer version;
}