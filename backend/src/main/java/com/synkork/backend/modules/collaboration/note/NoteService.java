package com.synkork.backend.modules.collaboration.note;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import com.synkork.backend.modules.collaboration.note.dto.NoteResponse;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class NoteService {
    
    @Autowired
    private NoteRepository noteRepository;

    public List<NoteResponse> getAllNotesBySpaceId(String spaceId) {
        UUID spaceUuid = UUID.fromString(spaceId);

        List<NoteEntity> notes = noteRepository.findBySpaceId(spaceUuid);

        return notes.stream().map(NoteResponse::new).collect(Collectors.toList());
    }

    public NoteResponse getNoteById(String noteId) {
        UUID noteUuid = UUID.fromString(noteId);

        NoteEntity note = noteRepository.findById(noteUuid).orElseThrow(() -> new IllegalArgumentException("Note not found"));

        return new NoteResponse(note);
    }

    
}
