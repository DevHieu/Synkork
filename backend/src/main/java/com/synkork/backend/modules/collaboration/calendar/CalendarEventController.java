package com.synkork.backend.modules.collaboration.calendar;

import com.synkork.backend.modules.collaboration.calendar.dto.CalendarEventDTO;
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
        List<CalendarEventDTO> conflicts = calendarEventService.findConflicts(spaceId, date, startTime, endTime,
                excludeId);
        return ResponseEntity.ok(conflicts);
    }

    // Tạo event mới
    @PostMapping
    public ResponseEntity<CalendarEventDTO> createEvent(@RequestBody CalendarEventDTO dto) {
        // Lấy userId từ sessionStorage phía frontend gửi lên
        String userId = dto.getCreatedById();
        if (userId == null || userId.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        CalendarEventDTO created = calendarEventService.createEvent(dto, userId);
        return ResponseEntity.ok(created);
    }

    // Cập nhật event
    @PutMapping("/{eventId}")
    public ResponseEntity<?> updateEvent(
            @PathVariable UUID eventId,
            @RequestBody CalendarEventDTO dto) {
        try {
            String userId = dto.getCreatedById();
            if (userId == null || userId.isEmpty()) {
                return ResponseEntity.badRequest().body("User ID is required");
            }
            CalendarEventDTO updated = calendarEventService.updateEvent(eventId, dto, userId);
            return ResponseEntity.ok(updated);
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Xóa event
    @DeleteMapping("/{eventId}")
    public ResponseEntity<?> deleteEvent(
            @PathVariable UUID eventId,
            @RequestParam String userId) {
        try {
            calendarEventService.deleteEvent(eventId, userId);
            return ResponseEntity.ok().build();
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
