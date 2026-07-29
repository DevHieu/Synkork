package com.synkork.backend.modules.collaboration.note;

import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import com.synkork.backend.modules.collaboration.note.dto.NoteRequest;
import com.synkork.backend.modules.collaboration.note.dto.NoteResponse;
import com.synkork.backend.modules.roomMember.RoomMemberEntity;
import com.synkork.backend.modules.roomMember.enums.RoomMemberRoleEnum;
import com.synkork.backend.modules.roomMember.RoomMemberRepository;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
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

    @Autowired
    private RoomMemberRepository roomMemberRepository;

    public List<NoteResponse> getAllNotesBySpaceId(String spaceId) {
        UUID spaceUuid = UUID.fromString(spaceId);
        List<NoteEntity> notes = noteRepository.findBySpaceIdAndArchived(spaceUuid, false);
        return notes.stream().map(NoteResponse::new).collect(Collectors.toList());
    }

    public NoteResponse getNoteById(String noteId) {
        UUID noteUuid = UUID.fromString(noteId);
        NoteEntity note = noteRepository.findById(noteUuid)
            .orElseThrow(() -> new IllegalArgumentException("Note not found"));
        return new NoteResponse(note);
    }

    public NoteResponse createNote(UUID spaceId, UUID userId, NoteRequest request) {
        SpaceEntity space = spaceRepository.findById(spaceId)
            .orElseThrow(() -> new IllegalArgumentException("Space not found"));

        UserEntity user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));

        NoteEntity note = NoteEntity.builder()
            .title(request.getTitle())
            .note(request.getNote())
            .pinned(Objects.requireNonNullElse(request.getPinned(), false))
            .color(request.getColor())
            .allowEditAll(Objects.requireNonNullElse(request.getAllowEditAll(), true))
            .createdBy(user)
            .space(space)
            .reminderAt(request.getReminderAt())
            .reminderSent(false)
            .posX(request.getPosX() != null ? request.getPosX() : 0)
            .posY(request.getPosY() != null ? request.getPosY() : 0)
            .width(request.getWidth() != null ? request.getWidth() : 3)
            .height(request.getHeight() != null ? request.getHeight() : 2)
            .archived(false)
            .build();

        return new NoteResponse(noteRepository.save(note));
    }

    public NoteResponse updateNote(String id, NoteRequest request) {
        UUID noteId = UUID.fromString(id);
        NoteEntity note = noteRepository.findById(noteId)
            .orElseThrow(() -> new RuntimeException("Note not found: " + id));

        if (request.getTitle() != null) note.setTitle(request.getTitle());
        if (request.getNote()  != null) note.setNote(request.getNote());
        if (request.getPinned() != null) note.setPinned(request.getPinned());
        if (request.getColor() != null) note.setColor(request.getColor());

        return new NoteResponse(noteRepository.save(note));
    }

    public void deleteNote(String id) {
        UUID uuid = UUID.fromString(id);
        if (!noteRepository.existsById(uuid)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Note not found");
        }
        noteRepository.deleteById(uuid);
    }

    public NoteResponse togglePin(String id) {
        UUID noteId = UUID.fromString(id);
        NoteEntity note = noteRepository.findById(noteId)
            .orElseThrow(() -> new RuntimeException("Note not found: " + id));

        note.setPinned(!note.getPinned());
        return new NoteResponse(noteRepository.save(note));
    }

    public NoteResponse toggleArchive(String id) {
        UUID noteId = UUID.fromString(id);
        NoteEntity note = noteRepository.findById(noteId)
            .orElseThrow(() -> new RuntimeException("Note not found: " + id));

        note.setArchived(!Boolean.TRUE.equals(note.getArchived()));
        return new NoteResponse(noteRepository.save(note));
    }

    public List<NoteResponse> searchNotes(String keyword) {
        return noteRepository.findByTitleContainingIgnoreCase(keyword)
            .stream().map(NoteResponse::new).collect(Collectors.toList());
    }

    public NoteResponse updatePosition(String id, NoteRequest request) {
        UUID noteId = UUID.fromString(id);
        NoteEntity note = noteRepository.findById(noteId)
            .orElseThrow(() -> new RuntimeException("Note not found: " + id));

        if (request.getPosX()   != null) note.setPosX(request.getPosX());
        if (request.getPosY()   != null) note.setPosY(request.getPosY());
        if (request.getWidth()  != null) note.setWidth(request.getWidth());
        if (request.getHeight() != null) note.setHeight(request.getHeight());

        return new NoteResponse(noteRepository.save(note));
    }

    public NoteResponse setReminder(String id, NoteRequest request) {
        UUID noteId = UUID.fromString(id);
        NoteEntity note = noteRepository.findById(noteId)
            .orElseThrow(() -> new RuntimeException("Note not found: " + id));

        note.setReminderAt(request.getReminderAt());
        note.setReminderSent(false);

        return new NoteResponse(noteRepository.save(note));
    }

    public List<NoteEntity> getPendingReminders() {
        return noteRepository.findPendingReminders(Instant.now());
    }

    public void markReminderSent(NoteEntity note) {
        note.setReminderSent(true);
        noteRepository.save(note);
    }

    public NoteResponse copyNoteToPersonalSpace(String noteId, UUID userId) {
        UUID noteUuid = UUID.fromString(noteId);
        NoteEntity source = noteRepository.findById(noteUuid)
            .orElseThrow(() -> new IllegalArgumentException("Note not found"));
    
        UserEntity user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
    
        if (user.getPersonalNoteId() == null) {
            throw new IllegalStateException("User chưa có personal note space");
        }
    
        SpaceEntity personalSpace = spaceRepository.findById(user.getPersonalNoteId())
            .orElseThrow(() -> new IllegalArgumentException("Personal space not found"));
    
        NoteEntity clone = NoteEntity.builder()
            .title(source.getTitle())
            .note(source.getNote())
            .pinned(false)
            .color(source.getColor())
            .allowEditAll(true)
            .createdBy(user)
            .space(personalSpace)
            .reminderAt(null)          
            .reminderSent(false)
            .posX(0)
            .posY(0)
            .width(source.getWidth())
            .height(source.getHeight())
            .archived(false)
            .build();
    
        return new NoteResponse(noteRepository.save(clone));
    }

    public List<NoteResponse> getArchivedNotesBySpaceId(String spaceId) {
        UUID spaceUuid = UUID.fromString(spaceId);
        List<NoteEntity> notes = noteRepository.findBySpaceIdAndArchived(spaceUuid, true);
        return notes.stream().map(NoteResponse::new).collect(Collectors.toList());
    }

    public void checkCanManageArchive(String spaceId, UUID userId) {
        UUID spaceUuid = UUID.fromString(spaceId);
        SpaceEntity space = spaceRepository.findById(spaceUuid)
            .orElseThrow(() -> new IllegalArgumentException("Space not found"));
    
        UUID roomId = space.getRoom().getId();
    
        RoomMemberEntity member = roomMemberRepository.findByRoom_IdAndUser_Id(roomId, userId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Bạn không phải thành viên của room này"));
    
        if (member.getRole() != RoomMemberRoleEnum.OWNER && member.getRole() != RoomMemberRoleEnum.ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Chỉ Owner hoặc Admin mới được quản lý ghi chú lưu trữ");
        }
    }
}