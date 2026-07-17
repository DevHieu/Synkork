package com.synkork.backend.modules.collaboration.calendar.dto;

import com.synkork.backend.modules.collaboration.calendar.entity.CalendarEventEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CalendarEventDTO {
    private UUID id;
    private String spaceId;
    private String title;
    private String description;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate eventDate;

    @JsonFormat(pattern = "HH:mm:ss")
    private LocalTime startTime;

    @JsonFormat(pattern = "HH:mm:ss")
    private LocalTime endTime;

    private String recurrenceType;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate recurrenceEndDate;
    private boolean allowEditAll;
    private Integer remindBeforeMinutes;

    private String createdById;
    private String createdByUsername;
    private String createdByDisplayName;
    private String createdByAvatarUrl;
    private String callRoomSpaceId;
    private String callRoomSpaceName;
    private String taskSpaceId;
    private String taskId;
    private String taskName;
    private String noteSpaceId;
    private String noteId;
    private String noteTitle;
    private List<String> attendeeIds = new ArrayList<>();
    private List<CalendarEventAttendeeDTO> attendees = new ArrayList<>();
    private List<CalendarEventAttachmentDTO> attachments = new ArrayList<>();

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Constructor từ Entity sang DTO
    public CalendarEventDTO(CalendarEventEntity entity) {
        this.id = entity.getId();
        this.spaceId = entity.getSpace().getId().toString();
        this.title = entity.getTitle();
        this.description = entity.getDescription();
        this.eventDate = entity.getEventDate();
        this.startTime = entity.getStartTime();
        this.endTime = entity.getEndTime();
        this.recurrenceType = entity.getRecurrenceType();
        this.recurrenceEndDate = entity.getRecurrenceEndDate();
        this.allowEditAll = entity.isAllowEditAll();
        this.remindBeforeMinutes = entity.getRemindBeforeMinutes();
        this.createdById = entity.getCreatedBy().getId().toString();
        this.createdByUsername = entity.getCreatedBy().getUsername();
        this.createdByDisplayName = entity.getCreatedBy().getDisplayName();
        this.createdByAvatarUrl = entity.getCreatedBy().getAvatarUrl();
        if (entity.getCallRoomSpace() != null) {
            this.callRoomSpaceId = entity.getCallRoomSpace().getId().toString();
            this.callRoomSpaceName = entity.getCallRoomSpace().getName();
        }
        if (entity.getTask() != null) {
            this.taskId = entity.getTask().getId().toString();
            this.taskName = entity.getTask().getTitle();
            if (entity.getTask().getColumn() != null && entity.getTask().getColumn().getSpace() != null) {
                this.taskSpaceId = entity.getTask().getColumn().getSpace().getId().toString();
            }
        }
        if (entity.getNote() != null) {
            this.noteId = entity.getNote().getId().toString();
            this.noteTitle = entity.getNote().getTitle();
            if (entity.getNote().getSpace() != null) {
                this.noteSpaceId = entity.getNote().getSpace().getId().toString();
            }
        }
        if (entity.getAttendees() != null) {
            for (var member : entity.getAttendees()) {
                this.attendeeIds.add(member.getId().toString());
                this.attendees.add(new CalendarEventAttendeeDTO(member));
            }
        }
        if (entity.getAttachments() != null) {
            for (var attachment : entity.getAttachments()) {
                this.attachments.add(new CalendarEventAttachmentDTO(attachment));
            }
        }
        this.createdAt = entity.getCreatedAt();
        this.updatedAt = entity.getUpdatedAt();
    }

    // Map ngược từ DTO sang Entity
    public void updateEntity(CalendarEventEntity target) {
        target.setTitle(this.title);
        target.setDescription(this.description);
        target.setEventDate(this.eventDate);
        target.setStartTime(this.startTime);
        target.setEndTime(this.endTime);
        target.setRecurrenceType(this.recurrenceType != null ? this.recurrenceType : "NONE");
        target.setRecurrenceEndDate(this.recurrenceEndDate);
        target.setAllowEditAll(this.allowEditAll);
        target.setRemindBeforeMinutes(this.remindBeforeMinutes);
    }
}
