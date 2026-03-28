package com.synkork.backend.modules.collaboration.calendar.dto;

import com.synkork.backend.modules.collaboration.calendar.CalendarEventEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CalendarEventDTO {
    private UUID id;
    private String spaceId;
    private String title;
    private String description;
    private LocalDate eventDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private String recurrenceType;
    private LocalDate recurrenceEndDate;
    private boolean allowEditAll;

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
        this.createdById = entity.getCreatedBy().getId().toString();
        this.createdByUsername = entity.getCreatedBy().getUsername();
        this.createdByDisplayName = entity.getCreatedBy().getDisplayName();
        this.createdAt = entity.getCreatedAt();
        this.updatedAt = entity.getUpdatedAt();
    }
}
