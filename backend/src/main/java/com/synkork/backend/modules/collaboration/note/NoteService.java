package com.synkork.backend.modules.collaboration.note;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import com.synkork.backend.modules.collaboration.note.dto.NoteRequest;
import com.synkork.backend.modules.collaboration.note.dto.NoteResponse;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.synkork.backend.modules.space.SpaceRepository;
import com.synkork.backend.modules.user.UserRepository;
import com.synkork.backend.modules.space.SpaceEntity;
import com.synkork.backend.modules.user.UserEntity;

@Service
public class NoteService {
    
    @Autowired
    private NoteRepository noteRepository;

    @Autowired
    private SpaceRepository spaceRepository;

    @Autowired
    private UserRepository userRepository;

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

    public NoteResponse createNote(UUID spaceId, UUID userId, NoteRequest request) {

        SpaceEntity space = spaceRepository.findById(spaceId).orElseThrow(() -> new IllegalArgumentException("Space not found"));
        UserEntity user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found"));

        NoteEntity note = new NoteEntity();
        note.setTitle(request.getTitle());
        note.setNote(request.getNote());
        note.setImportant(request.getImportant());
        note.setColor(request.getColor());
        note.setAllowEditAll(request.getAllowEditAll());
        note.setCreatedBy(user);
        note.setSpace(space);

        NoteEntity saved = noteRepository.save(note);

        return new NoteResponse(saved);
    }

    

}
