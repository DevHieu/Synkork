package com.synkork.backend.modules.collaboration.calendar;

import com.synkork.backend.modules.collaboration.calendar.dto.CalendarEventDTO;
import com.synkork.backend.modules.space.SpaceRepository;
import com.synkork.backend.modules.user.UserEntity;
import com.synkork.backend.modules.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CalendarEventService {

    @Autowired
    private CalendarEventRepository calendarEventRepository;

    @Autowired
    private SpaceRepository spaceRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    // Broadcast thay đổi lịch qua WebSocket
    private void broadcastCalendarUpdate(String spaceId, String action, CalendarEventDTO event) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("action", action);
        payload.put("event", event);
        messagingTemplate.convertAndSend("/topic/space/" + spaceId + "/calendar", payload);
    }

    // Lấy tất cả event theo spaceId
    public List<CalendarEventDTO> getEventsBySpaceId(UUID spaceId) {
        List<CalendarEventEntity> events = calendarEventRepository.findBySpaceId(spaceId);
        return events.stream().map(CalendarEventDTO::new).collect(Collectors.toList());
    }

    // Lấy event theo khoảng thời gian (dùng cho tuần/tháng/năm)
    public List<CalendarEventDTO> getEventsByDateRange(UUID spaceId, LocalDate start, LocalDate end) {
        List<CalendarEventEntity> events = calendarEventRepository.findBySpaceIdAndEventDateBetween(spaceId, start,
                end);
        return events.stream().map(CalendarEventDTO::new).collect(Collectors.toList());
    }

    // Lấy event theo ngày cụ thể
    public List<CalendarEventDTO> getEventsByDate(UUID spaceId, LocalDate date) {
        List<CalendarEventEntity> events = calendarEventRepository.findBySpaceIdAndEventDate(spaceId, date);
        return events.stream().map(CalendarEventDTO::new).collect(Collectors.toList());
    }

    // Tạo event mới
    public CalendarEventDTO createEvent(CalendarEventDTO dto, String userId) {
        UUID userUUID = UUID.fromString(userId);
        UserEntity user = userRepository.findById(userUUID)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        CalendarEventEntity entity = new CalendarEventEntity();
        entity.setSpace(spaceRepository.getReferenceById(UUID.fromString(dto.getSpaceId())));
        entity.setTitle(dto.getTitle());
        entity.setDescription(dto.getDescription());
        entity.setEventDate(dto.getEventDate());
        entity.setStartTime(dto.getStartTime());
        entity.setEndTime(dto.getEndTime());
        entity.setAllowEditAll(dto.isAllowEditAll());
        entity.setCreatedBy(user);

        CalendarEventEntity saved = calendarEventRepository.save(entity);
        CalendarEventDTO result = new CalendarEventDTO(saved);
        broadcastCalendarUpdate(dto.getSpaceId(), "CREATED", result);
        return result;
    }

    // Cập nhật event (kiểm tra quyền: creator hoặc allowEditAll)
    public CalendarEventDTO updateEvent(UUID eventId, CalendarEventDTO dto, String userId) {
        CalendarEventEntity entity = calendarEventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event not found"));

        UUID userUUID = UUID.fromString(userId);

        // Kiểm tra quyền: chỉ creator hoặc allowEditAll mới được sửa
        if (!entity.getCreatedBy().getId().equals(userUUID) && !entity.isAllowEditAll()) {
            throw new SecurityException("Bạn không có quyền chỉnh sửa sự kiện này");
        }

        entity.setTitle(dto.getTitle());
        entity.setDescription(dto.getDescription());
        entity.setEventDate(dto.getEventDate());
        entity.setStartTime(dto.getStartTime());
        entity.setEndTime(dto.getEndTime());
        entity.setAllowEditAll(dto.isAllowEditAll());

        CalendarEventEntity saved = calendarEventRepository.save(entity);
        CalendarEventDTO result = new CalendarEventDTO(saved);
        broadcastCalendarUpdate(result.getSpaceId(), "UPDATED", result);
        return result;
    }

    // Xóa event (chỉ creator)
    public void deleteEvent(UUID eventId, String userId) {
        CalendarEventEntity entity = calendarEventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event not found"));

        UUID userUUID = UUID.fromString(userId);

        if (!entity.getCreatedBy().getId().equals(userUUID)) {
            throw new SecurityException("Bạn không có quyền xóa sự kiện này");
        }

        // Broadcast trước khi xóa
        CalendarEventDTO deletedDto = new CalendarEventDTO(entity);
        calendarEventRepository.delete(entity);
        broadcastCalendarUpdate(deletedDto.getSpaceId(), "DELETED", deletedDto);
    }

    // Kiểm tra sự kiện trùng giờ
    public List<CalendarEventDTO> findConflicts(UUID spaceId, LocalDate date, LocalTime startTime, LocalTime endTime,
            UUID excludeEventId) {
        List<CalendarEventEntity> sameDayEvents = calendarEventRepository.findBySpaceIdAndEventDate(spaceId, date);
        return sameDayEvents.stream()
                .filter(e -> excludeEventId == null || !e.getId().equals(excludeEventId))
                .filter(e -> e.getStartTime().isBefore(endTime) && e.getEndTime().isAfter(startTime))
                .map(CalendarEventDTO::new)
                .collect(Collectors.toList());
    }
}
