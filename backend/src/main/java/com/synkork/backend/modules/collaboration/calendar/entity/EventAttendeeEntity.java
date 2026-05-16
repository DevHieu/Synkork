package com.synkork.backend.modules.collaboration.calendar.entity;

import com.synkork.backend.common.base.BaseEntity;
import com.synkork.backend.modules.user.UserEntity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "event_attendees",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"event_id", "user_id"})
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EventAttendeeEntity extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false, columnDefinition = "BINARY(16)")
    private CalendarEventEntity event;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, columnDefinition = "BINARY(16)")
    private UserEntity user;
}
