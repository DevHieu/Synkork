package com.synkork.backend.modules.collaboration.calendar.entity;

import com.synkork.backend.common.base.BaseEntity;
import com.synkork.backend.modules.space.SpaceEntity;
import com.synkork.backend.modules.user.UserEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name="calendar_events")
@Getter
@Setter
@AllArgsConstructor
public class CalendarEventEntity extends BaseEntity {

    @ManyToOne( fetch = FetchType.LAZY)
    @JoinColumn(name= "space_id", nullable = false, columnDefinition = "BINARY(16)")
    private SpaceEntity space;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String description;

    private String eventLink;

    @Column(nullable = false)
    private LocalDate eventDate;

    private LocalDate endDate;

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

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    @Setter(AccessLevel.NONE)
    private List<EventAttendeeEntity> attendees;

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    @Setter(AccessLevel.NONE)
    private List<EventAttachmentEntity> attachments;

    public CalendarEventEntity() {
        this.attendees = new ArrayList<>();
        this.attachments = new ArrayList<>();
    }

    public void replaceAttendees(Collection<EventAttendeeEntity> newAttendees) {
        if (attendees == null) {
            attendees = new ArrayList<>();
        }
        attendees.clear();
        if (newAttendees == null) {
            return;
        }

        for (EventAttendeeEntity attendee : newAttendees) {
            if (attendee == null) {
                continue;
            }
            attendee.setEvent(this);
            attendees.add(attendee);
        }
    }

    public void replaceAttachments(Collection<EventAttachmentEntity> newAttachments) {
        if (attachments == null) {
            attachments = new ArrayList<>();
        }
        attachments.clear();
        if (newAttachments == null) {
            return;
        }

        for (EventAttachmentEntity attachment : newAttachments) {
            if (attachment == null) {
                continue;
            }
            attachment.setEvent(this);
            attachments.add(attachment);
        }
    }


}
