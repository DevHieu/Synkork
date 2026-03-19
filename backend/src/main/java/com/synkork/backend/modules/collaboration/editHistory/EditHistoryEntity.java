package com.synkork.backend.modules.collaboration.editHistory;

import com.synkork.backend.modules.user.UserEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "edit_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EditHistoryEntity {
    @Id
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @Enumerated(EnumType.STRING)
    private ResourceTypeEnum resourceType; // e.g., "CARD", "NOTE"

    @Column(columnDefinition = "BINARY(16)")
    private UUID resourceId;

    private String action;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "edited_by", nullable = false)
    private UserEntity editedBy;

    private LocalDateTime editedAt;
}
