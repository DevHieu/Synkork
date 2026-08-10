package com.synkork.backend.modules.collaboration.calendar.entity;

import com.synkork.backend.modules.collaboration.task.card.CardEntity;
import com.synkork.backend.modules.collaboration.note.NoteEntity;
import com.synkork.backend.common.base.BaseEntity;
import com.synkork.backend.modules.roomMember.RoomMemberEntity;
import com.synkork.backend.modules.space.SpaceEntity;
import com.synkork.backend.modules.user.UserEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import com.synkork.backend.modules.collaboration.calendar.enums.SyncStatus;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name="calendar_events")
@Getter
@Setter
@AllArgsConstructor
public class CalendarEventEntity extends BaseEntity {

    @Version
    private Integer version;

    @ManyToOne( fetch = FetchType.LAZY)
    @JoinColumn(name= "space_id", nullable = false, columnDefinition = "BINARY(16)")
    private SpaceEntity space;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "call_room_space_id", columnDefinition = "BINARY(16)", nullable = true)
    private SpaceEntity callRoomSpace;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", columnDefinition = "BINARY(16)", nullable = true)
    private CardEntity task;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "note_id", columnDefinition = "BINARY(16)", nullable = true)
    private NoteEntity note;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private LocalDate eventDate;

    private LocalDate endDate;

    @Column(nullable = false)
    private LocalTime startTime;

    @Column(nullable = false)
    private LocalTime endTime;

    private String recurrenceType;
    private LocalDate recurrenceEndDate;

    private boolean schedule;
    private UUID scheduleId;

    private boolean allowEditAll;
    private Integer remindBeforeMinutes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="created_by",nullable = false, columnDefinition = "BINARY(16)")
    private UserEntity createdBy;

    // Google Calendar Sync Fields
    private String googleEventId;
    private String googleCalendarId = "primary";
    
    @Enumerated(EnumType.STRING)
    private SyncStatus syncStatus = SyncStatus.PENDING;
    
    private LocalDateTime lastSyncedAt;

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
    @org.hibernate.annotations.Fetch(org.hibernate.annotations.FetchMode.SUBSELECT)
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
