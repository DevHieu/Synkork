package com.synkork.backend.modules.collaboration.note;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;

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
import org.springframework.web.bind.annotation.PutMapping;





@RestController
@RequestMapping("/spaces/{spaceId}/notes")
public class NoteController {

    @Autowired
    private NoteService noteService;

    @Autowired
    private SimpMessagingTemplate messageTemplate;

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

        NoteResponse response = noteService.createNote(UUID.fromString(spaceId), userId, request);

        messageTemplate.convertAndSend("/topic/space/" + spaceId + "/notes/create", response);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<NoteResponse> updateNote(
            @PathVariable String id, @RequestBody NoteRequest request) {
        return ResponseEntity.ok(noteService.updateNote(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void>deleteNote(@PathVariable String id) {
        noteService.deleteNote(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/pin")
    public ResponseEntity<NoteResponse> togglePin(@PathVariable String id) {
        return ResponseEntity.ok(noteService.togglePin(id));
    }

    @GetMapping("/search")
    public ResponseEntity<List<NoteResponse>> search(@RequestParam String keyword) {
        return ResponseEntity.ok(noteService.searchNotes(keyword));
    }
}