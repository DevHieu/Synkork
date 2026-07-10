package com.synkork.backend.modules.collaboration.calendar.entity;

import com.synkork.backend.common.base.BaseEntity;
import com.synkork.backend.modules.roomMember.RoomMemberEntity;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "call_room_space_id", columnDefinition = "BINARY(16)", nullable = true)
    private SpaceEntity callRoomSpace;

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

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "calendar_event_room_members",
            joinColumns = @JoinColumn(name = "event_id"),
            inverseJoinColumns = @JoinColumn(name = "room_member_id"),
            uniqueConstraints = @UniqueConstraint(columnNames = {"event_id", "room_member_id"})
    )
    @Setter(AccessLevel.NONE)
    private List<RoomMemberEntity> attendees;

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    @Setter(AccessLevel.NONE)
    private List<EventAttachmentEntity> attachments;

    public CalendarEventEntity() {
        this.attendees = new ArrayList<>();
        this.attachments = new ArrayList<>();
    }

    public void replaceAttendees(Collection<RoomMemberEntity> newAttendees) {
        if (attendees == null) {
            attendees = new ArrayList<>();
        }
        attendees.clear();
        if (newAttendees == null) {
            return;
        }

        for (RoomMemberEntity attendee : newAttendees) {
            if (attendee == null) {
                continue;
            }
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
