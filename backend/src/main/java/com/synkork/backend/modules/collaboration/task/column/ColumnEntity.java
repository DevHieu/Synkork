package com.synkork.backend.modules.collaboration.task.column;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.synkork.backend.modules.collaboration.task.card.CardEntity;
import com.synkork.backend.modules.space.SpaceEntity;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(
        name = "columns",
        indexes = {
                @Index(name = "idx_columns_space_position", columnList = "space_id, position")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ColumnEntity {

    @Id
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "space_id", nullable = false, columnDefinition = "BINARY(16)")
    @JsonIgnore
    private SpaceEntity space;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false)
    private int position;

    @Column(nullable = false)
    private Boolean archived = false;

    @Column(name = "archived_at")
    private LocalDateTime archivedAt;

    @OneToMany(mappedBy = "column", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("position ASC")
    private Set<CardEntity> cards = new HashSet<>();

    @Version
    private Integer version;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ColumnEntity that = (ColumnEntity) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}