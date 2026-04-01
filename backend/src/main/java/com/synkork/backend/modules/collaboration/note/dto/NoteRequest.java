package com.synkork.backend.modules.collaboration.note.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NoteRequest {
    private String title;
    private String note;
    private Boolean important;
    private String color;
    private Boolean allowEditAll;
    
}
