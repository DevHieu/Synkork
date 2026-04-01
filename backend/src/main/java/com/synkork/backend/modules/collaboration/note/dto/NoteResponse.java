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
    private Boolean important;
    private String color;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String displayName;
    private String avatarUrl;

    public NoteResponse(NoteEntity note) {
        this.id = note.getId();
        this.title = note.getTitle();
        this.note = note.getNote();
        this.important = note.getImportant();
        this.color = note.getColor();
        this.createdAt = note.getCreatedAt();
        this.updatedAt = note.getUpdatedAt();
    }
}
