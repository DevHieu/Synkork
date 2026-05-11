package com.synkork.backend.modules.collaboration.calendar.controller;

import com.synkork.backend.modules.collaboration.calendar.repository.CalendarEventRepository;
import com.synkork.backend.modules.collaboration.calendar.service.CalendarEventService;
import com.synkork.backend.modules.collaboration.calendar.dto.CalendarEventDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime endTime,
            @RequestParam(required = false) UUID excludeId) {
        List<CalendarEventDTO> conflicts = calendarEventService.findConflicts(spaceId, date, startTime, endTime, excludeId);
        return ResponseEntity.ok(conflicts);
    }

    // Thêm hàm xác thực logic nhỏ gọn
    private void validateUserId(String userId) {
        if (userId == null || userId.isEmpty()) {
            throw new IllegalArgumentException("User ID is required");
        }
    }

    // Tạo event mới
    @PostMapping
    public ResponseEntity<CalendarEventDTO> createEvent(@RequestBody CalendarEventDTO dto) {
        String userId = dto.getCreatedById();
        validateUserId(userId);
        
        CalendarEventDTO created = calendarEventService.createEvent(dto, userId);
        return ResponseEntity.ok(created);
    }

    // Cập nhật event
    @PutMapping("/{eventId}")
    public ResponseEntity<CalendarEventDTO> updateEvent(
            @PathVariable UUID eventId,
            @RequestBody CalendarEventDTO dto) {
        String userId = dto.getCreatedById();
        validateUserId(userId);
                
        CalendarEventDTO updated = calendarEventService.updateEvent(eventId, dto, userId);
        return ResponseEntity.ok(updated);
    }

    // Xóa event
    @DeleteMapping("/{eventId}")
    public ResponseEntity<Void> deleteEvent(
            @PathVariable UUID eventId,
            @RequestParam String userId) {
        validateUserId(userId);
        
        calendarEventService.deleteEvent(eventId, userId);
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
