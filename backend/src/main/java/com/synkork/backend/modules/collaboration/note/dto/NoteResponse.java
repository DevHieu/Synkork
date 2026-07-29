package com.synkork.backend.modules.collaboration.note.dto;

import java.util.UUID;
import java.time.Instant;
import java.time.LocalDateTime;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import com.synkork.backend.modules.collaboration.note.NoteEntity;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NoteResponse {
    private UUID id;
    private UUID spaceId;
    private String title;
    private String note;
    private Boolean pinned;
    private String color;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String displayName;
    private String avatarUrl;
    private Integer posX;
    private Integer posY;
    private Integer width;
    private Integer height;
    private Instant reminderAt;
    private Boolean reminderSent;
    private Boolean archived;

    public NoteResponse(NoteEntity note) {
        this.id = note.getId();
        this.spaceId = note.getSpace().getId();
        this.title = note.getTitle();
        this.note = note.getNote();
        this.archived = note.getArchived(); 
        this.pinned = note.getPinned();
        this.color = note.getColor();
        this.createdAt = note.getCreatedAt();
        this.updatedAt = note.getUpdatedAt();
        this.posX = note.getPosX();
        this.posY = note.getPosY();
        this.width = note.getWidth();
        this.height = note.getHeight();
        this.reminderAt = note.getReminderAt();
        this.reminderSent = note.getReminderSent();
        this.displayName = note.getCreatedBy().getDisplayName();
        this.avatarUrl = note.getCreatedBy().getAvatarUrl();
    }
}