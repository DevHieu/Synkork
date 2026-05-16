package com.synkork.backend.modules.collaboration.calendar.entity;

import com.synkork.backend.common.base.BaseEntity;
import com.synkork.backend.modules.space.SpaceEntity;
import com.synkork.backend.modules.user.UserEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name="calendar_events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CalendarEventEntity extends BaseEntity {

    @ManyToOne( fetch = FetchType.LAZY)
    @JoinColumn(name= "space_id", nullable = false, columnDefinition = "BINARY(16)")
    private SpaceEntity space;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private LocalDate eventDate;

    @Column(nullable = false)
    private LocalTime startTime;

    @Column(nullable = false)
    private LocalTime endTime;

    private String recurrenceType;
    private LocalDate recurrenceEndDate;

    private boolean allowEditAll;
    private Integer remindBeforeMinutes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="created_by",nullable = false, columnDefinition = "BINARY(16)")
    private UserEntity createdBy;

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL)
    private List<EventAttendeeEntity> attendees;

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL)
    private List<EventAttachmentEntity> attachments;


}
