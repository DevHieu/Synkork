package com.synkork.backend.modules.collaboration.calendar.controller;

import com.synkork.backend.modules.collaboration.calendar.repository.CalendarEventRepository;
import com.synkork.backend.modules.collaboration.calendar.service.CalendarEventService;
import com.synkork.backend.modules.collaboration.calendar.dto.CalendarEventDTO;
import com.synkork.backend.common.utils.AuthUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.synkork.backend.modules.collaboration.calendar.dto.CalendarEventAttachmentDTO;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/calendar-events")
public class CalendarEventController {

    @Autowired
    private CalendarEventService calendarEventService;

    // Lấy tất cả event theo spaceId
    @GetMapping("/{spaceId}")
    public ResponseEntity<List<CalendarEventDTO>> getEventsBySpaceId(@PathVariable UUID spaceId) {
        List<CalendarEventDTO> events = calendarEventService.getEventsBySpaceId(spaceId);
        return ResponseEntity.ok(events);
    }

    // Lấy event theo khoảng thời gian
    @GetMapping("/{spaceId}/range")
    public ResponseEntity<List<CalendarEventDTO>> getEventsByDateRange(
            @PathVariable UUID spaceId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        List<CalendarEventDTO> events = calendarEventService.getEventsByDateRange(spaceId, start, end);
        return ResponseEntity.ok(events);
    }

    // Lấy event theo ngày cụ thể
    @GetMapping("/{spaceId}/date")
    public ResponseEntity<List<CalendarEventDTO>> getEventsByDate(
            @PathVariable UUID spaceId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<CalendarEventDTO> events = calendarEventService.getEventsByDate(spaceId, date);
        return ResponseEntity.ok(events);
    }

    // Kiểm tra sự kiện trùng giờ
    @GetMapping("/{spaceId}/conflicts")
    public ResponseEntity<List<CalendarEventDTO>> checkConflicts(
            @PathVariable UUID spaceId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam @DateTimeFormat(pattern = "HH:mm") LocalTime startTime,
            @RequestParam @DateTimeFormat(pattern = "HH:mm") LocalTime endTime,
            @RequestParam(required = false) UUID excludeId) {
        List<CalendarEventDTO> conflicts = calendarEventService.findConflicts(spaceId, date, startTime, endTime, excludeId);
        return ResponseEntity.ok(conflicts);
    }

    // Tạo event mới
    @PostMapping
    public ResponseEntity<CalendarEventDTO> createEvent(@RequestBody CalendarEventDTO dto) {
        if (dto.getSpaceId() == null || dto.getSpaceId().isEmpty()) {
            throw new IllegalArgumentException("Space ID is required");
        }
        UUID userId = AuthUtils.getCurrentUserId();
        CalendarEventDTO created = calendarEventService.createEvent(dto, userId);
        return ResponseEntity.ok(created);
    }

    // Cập nhật event
    @PutMapping("/{eventId}")
    public ResponseEntity<CalendarEventDTO> updateEvent(
            @PathVariable UUID eventId,
            @RequestBody CalendarEventDTO dto) {
        UUID userId = AuthUtils.getCurrentUserId();
        CalendarEventDTO updated = calendarEventService.updateEvent(eventId, dto, userId);
        return ResponseEntity.ok(updated);
    }

    // Xóa event
    @DeleteMapping("/{eventId}")
    public ResponseEntity<Void> deleteEvent(@PathVariable UUID eventId) {
        UUID userId = AuthUtils.getCurrentUserId();
        calendarEventService.deleteEvent(eventId, userId);
        return ResponseEntity.ok().build();
    }


    @PostMapping("/{eventId}/attachments")
    public ResponseEntity<List<CalendarEventAttachmentDTO>> uploadAttachments(
            @PathVariable UUID eventId,
            @RequestParam("files") List<MultipartFile> files) {
        UUID userId = AuthUtils.getCurrentUserId();
        List<CalendarEventAttachmentDTO> uploaded = calendarEventService.uploadAttachments(eventId, files, userId);
        return ResponseEntity.ok(uploaded);
    }


    @PostMapping("/{eventId}/attachments/{attachmentId}/summarize")
    public ResponseEntity<String> summarizeAttachment(
            @PathVariable UUID eventId,
            @PathVariable UUID attachmentId) {
        UUID userId = AuthUtils.getCurrentUserId();
        return ResponseEntity.ok(calendarEventService.summarizeAttachment(eventId, attachmentId, userId));
    }

    @DeleteMapping("/{eventId}/attachments/{attachmentId}")
    public ResponseEntity<Void> deleteAttachment(
            @PathVariable UUID eventId,
            @PathVariable UUID attachmentId) {
        UUID userId = AuthUtils.getCurrentUserId();
        calendarEventService.deleteAttachment(eventId, attachmentId, userId);
        return ResponseEntity.ok().build();
    }

    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<String> handleSecurityException(SecurityException e) {
        return ResponseEntity.status(403).body(e.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }
}
