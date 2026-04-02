package com.synkork.backend.modules.collaboration.note;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import com.synkork.backend.security.UserPrinciple;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;

import com.synkork.backend.modules.collaboration.note.dto.NoteResponse;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import com.synkork.backend.modules.collaboration.note.dto.NoteRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import java.util.UUID;

@RestController
@RequestMapping("spaces/{spaceId}/notes/")
public class NoteController {

    @Autowired
    private NoteService noteService;

    @GetMapping
    public ResponseEntity<List<NoteResponse>> getAllNotes(@PathVariable String spaceId) {
        return ResponseEntity.ok(noteService.getAllNotesBySpaceId(spaceId));
    }

    @GetMapping("/{noteId}")
    public ResponseEntity<NoteResponse> getNoteById(@PathVariable String noteId) {
        return ResponseEntity.ok(noteService.getNoteById(noteId));
    }

    @PostMapping
    public ResponseEntity<NoteResponse> createNote(@PathVariable String spaceId, @RequestBody NoteRequest request) {
        UserPrinciple user = (UserPrinciple) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        
        UUID userId = user.getId();

        return ResponseEntity.ok(noteService.createNote(UUID.fromString(spaceId), userId, request));
    }
}