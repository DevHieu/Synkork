package com.synkork.backend.modules.collaboration.calendar.dto;

import com.synkork.backend.modules.collaboration.calendar.entity.CalendarEventEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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
