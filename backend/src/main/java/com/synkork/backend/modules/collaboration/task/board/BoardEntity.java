package com.synkork.backend.modules.collaboration.task.board;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.synkork.backend.modules.collaboration.task.column.ColumnEntity;
import com.synkork.backend.modules.space.SpaceEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "boards")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BoardEntity {
    @Id
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "space_id", nullable = false, columnDefinition = "BINARY(16)")
    private SpaceEntity space;

    @Column(nullable = false)
    private String name;

    @OneToMany(mappedBy = "board", cascade = CascadeType.ALL)
    @OrderBy("position ASC")
    @JsonManagedReference
    private List<ColumnEntity> columns;
}
