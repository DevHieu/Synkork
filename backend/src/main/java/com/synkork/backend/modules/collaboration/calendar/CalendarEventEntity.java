package com.synkork.backend.modules.collaboration.calendar;

import com.synkork.backend.common.base.BaseEntity;
import com.synkork.backend.modules.space.SpaceEntity;
import com.synkork.backend.modules.user.UserEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name="calendar_events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CalendarEventEntity extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "space_id", nullable = false)
    private SpaceEntity space;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    private LocalDate eventDate;
    private LocalTime startTime;
    private LocalTime endTime;

    private String recurrenceType; // NONE, DAILY, WEEKLY
    private LocalDate recurrenceEndDate;

    private boolean allowEditAll;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private UserEntity createdBy;
}
