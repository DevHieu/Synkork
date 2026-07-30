package com.synkork.backend.modules.collaboration.task.card;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.hibernate.annotations.UuidGenerator;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.synkork.backend.common.base.BaseEntity;
import com.synkork.backend.modules.collaboration.task.card.enums.CardStatus;
import com.synkork.backend.modules.collaboration.task.column.ColumnEntity;
import com.synkork.backend.modules.roomMember.RoomMemberEntity;
import com.synkork.backend.modules.user.UserEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "cards")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CardEntity extends BaseEntity {
    @Id
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "column_id", nullable = false)
    @JsonIgnore
    private ColumnEntity column;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "card_assignees",
        joinColumns = @JoinColumn(name = "card_id"),
        inverseJoinColumns = @JoinColumn(name = "room_member_id")
    )
    private List<RoomMemberEntity> assignees = new ArrayList<>();

    private int position;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false, columnDefinition = "BINARY(16)")
    private RoomMemberEntity createdBy;

    //thêm
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @Column(name = "due_date")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime dueDate;

    @Column(nullable = false)
    private Boolean overdueMailSent = false;

    @Column(nullable = false)
    private Boolean dueSoonMailSent = false;

    @Column(nullable = false)
    private Boolean archived = false;

    @Column(name = "archived_at")
    private LocalDateTime archivedAt;

    @JsonProperty("columnId") 
    public UUID getColumnId() {
        return column != null ? column.getId() : null;
    }

    public CardStatus getStatus() {

        if (dueDate == null) {
            return CardStatus.NORMAL;
        }

        LocalDateTime now = LocalDateTime.now();

        if (dueDate.isBefore(now)) {
            return CardStatus.OVERDUE;
        }

        long hours = Duration.between(now, dueDate).toHours();

        if (hours <= 24) {
            return CardStatus.DUE_SOON;
        }

        return CardStatus.NORMAL;
    }

    @Version
    private Integer version;
}
