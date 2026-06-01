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
    private String eventLink;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate eventDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

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
    private List<UUID> attendeeIds = new ArrayList<>();
    private List<CalendarEventAttendeeDTO> attendees = new ArrayList<>();
    private List<CalendarEventAttachmentDTO> attachments = new ArrayList<>();

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate displayDate;

    @JsonFormat(pattern = "HH:mm:ss")
    private LocalTime displayStartTime;

    @JsonFormat(pattern = "HH:mm:ss")
    private LocalTime displayEndTime;

    private boolean continuesFromPreviousDay;
    private boolean continuesToNextDay;
    private String originalStartDateTime;
    private String originalEndDateTime;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Constructor từ Entity sang DTO
    public CalendarEventDTO(CalendarEventEntity entity) {
        this.id = entity.getId();
        this.spaceId = entity.getSpace().getId().toString();
        this.title = entity.getTitle();
        this.description = entity.getDescription();
        this.eventLink = entity.getEventLink();
        this.eventDate = entity.getEventDate();
        this.endDate = entity.getEndDate() != null ? entity.getEndDate() : entity.getEventDate();
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
        if (entity.getAttendees() != null) {
            for (var attendee : entity.getAttendees()) {
                this.attendeeIds.add(attendee.getUser().getId());
                this.attendees.add(new CalendarEventAttendeeDTO(attendee.getUser()));
            }
        }
        if (entity.getAttachments() != null) {
            for (var attachment : entity.getAttachments()) {
                this.attachments.add(new CalendarEventAttachmentDTO(attachment));
            }
        }
        this.createdAt = entity.getCreatedAt();
        this.updatedAt = entity.getUpdatedAt();
        this.displayDate = this.eventDate;
        this.displayStartTime = this.startTime;
        this.displayEndTime = this.endTime;
        this.originalStartDateTime = this.eventDate + "T" + this.startTime;
        this.originalEndDateTime = this.endDate + "T" + this.endTime;
    }

    // Map ngược từ DTO sang Entity
    public void updateEntity(CalendarEventEntity target) {
        target.setTitle(this.title);
        target.setDescription(this.description);
        target.setEventLink(normalizeEventLink(this.eventLink));
        target.setEventDate(this.eventDate);
        target.setEndDate(this.endDate != null ? this.endDate : this.eventDate);
        target.setStartTime(this.startTime);
        target.setEndTime(this.endTime);
        target.setRecurrenceType(this.recurrenceType != null ? this.recurrenceType : "NONE");
        target.setRecurrenceEndDate(this.recurrenceEndDate);
        target.setAllowEditAll(this.allowEditAll);
        target.setRemindBeforeMinutes(this.remindBeforeMinutes);
    }

    private String normalizeEventLink(String eventLink) {
        if (eventLink == null || eventLink.trim().isEmpty()) {
            return null;
        }
        return eventLink.trim();
    }
}
