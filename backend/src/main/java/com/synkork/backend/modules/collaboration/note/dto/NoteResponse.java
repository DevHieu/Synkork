package com.synkork.backend.modules.collaboration.note.dto;

import java.util.UUID;
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

    public NoteResponse(NoteEntity note) {
        this.id = note.getId();
        this.title = note.getTitle();
        this.note = note.getNote();
        this.pinned = note.getPinned();
        this.color = note.getColor();
        this.createdAt = note.getCreatedAt();
        this.updatedAt = note.getUpdatedAt();
        this.posX   = note.getPosX();
        this.posY   = note.getPosY();
        this.width  = note.getWidth();
        this.height = note.getHeight();
    }
}
