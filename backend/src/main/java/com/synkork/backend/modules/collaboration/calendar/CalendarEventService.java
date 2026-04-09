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

    private static final int DEFAULT_RECURRENCE_LIMIT_YEARS = 1;
    private static final String RECURRENCE_NONE = "NONE";
    private static final String RECURRENCE_DAILY = "DAILY";
    private static final String RECURRENCE_WEEKLY = "WEEKLY";
    private static final String RECURRENCE_MONTHLY = "MONTHLY";
    private static final String RECURRENCE_YEARLY = "YEARLY";

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
        List<CalendarEventEntity> allSpaceEvents = calendarEventRepository.findBySpaceId(spaceId);
        List<CalendarEventDTO> expandedResults = new java.util.ArrayList<>();

        for (CalendarEventEntity event : allSpaceEvents) {
            if (isNonRecurring(event)) {
                addIfInRange(expandedResults, event, start, end);
            } else {
                expandedResults.addAll(expandRecurringEvent(event, start, end));
            }
        }

        return expandedResults;
    }

    private boolean isNonRecurring(CalendarEventEntity event) {
        return RECURRENCE_NONE.equals(event.getRecurrenceType()) || event.getRecurrenceType() == null;
    }

    private void addIfInRange(List<CalendarEventDTO> results, CalendarEventEntity event, LocalDate start,
            LocalDate end) {
        LocalDate eventDate = event.getEventDate();
        if (!eventDate.isBefore(start) && !eventDate.isAfter(end)) {
            results.add(new CalendarEventDTO(event));
        }
    }

    private List<CalendarEventDTO> expandRecurringEvent(CalendarEventEntity event, LocalDate rangeStart,
            LocalDate rangeEnd) {
        List<CalendarEventDTO> instances = new java.util.ArrayList<>();
        LocalDate currentOccurrence = event.getEventDate();
        LocalDate recurrenceLimit = calculateRecurrenceLimit(event);

        if (currentOccurrence.isAfter(rangeEnd)) {
            return instances;
        }

        while (!currentOccurrence.isAfter(recurrenceLimit) && !currentOccurrence.isAfter(rangeEnd)) {
            if (!currentOccurrence.isBefore(rangeStart)) {
                instances.add(createVirtualInstance(event, currentOccurrence));
            }
            currentOccurrence = getNextOccurrence(currentOccurrence, event.getRecurrenceType());
            if (currentOccurrence == null)
                break;
        }
        return instances;
    }

    private LocalDate calculateRecurrenceLimit(CalendarEventEntity event) {
        if (event.getRecurrenceEndDate() != null) {
            return event.getRecurrenceEndDate();
        }
        return event.getEventDate().plusYears(DEFAULT_RECURRENCE_LIMIT_YEARS);
    }

    private CalendarEventDTO createVirtualInstance(CalendarEventEntity original, LocalDate date) {
        CalendarEventDTO instance = new CalendarEventDTO(original);
        instance.setEventDate(date);
        return instance;
    }

    private LocalDate getNextOccurrence(LocalDate current, String type) {
        if (RECURRENCE_DAILY.equals(type)) {
            return current.plusDays(1);
        } else if (RECURRENCE_WEEKLY.equals(type)) {
            return current.plusWeeks(1);
        } else if (RECURRENCE_MONTHLY.equals(type)) {
            return current.plusMonths(1);
        } else if (RECURRENCE_YEARLY.equals(type)) {
            return current.plusYears(1);
        }
        return null;
    }

    // Lấy event theo ngày cụ thể (Bao gồm cả các sự kiện lặp)
    public List<CalendarEventDTO> getEventsByDate(UUID spaceId, LocalDate date) {
        return getEventsByDateRange(spaceId, date, date);
    }

    private void validateEventTime(CalendarEventDTO eventRequest) {
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        if (eventRequest.getEventDate().isBefore(today)) {
            throw new IllegalArgumentException("Không thể tạo sự kiện ở quá khứ");
        }

        if (eventRequest.getEventDate().isEqual(today) && eventRequest.getStartTime().isBefore(now)) {
            throw new IllegalArgumentException("Không thể tạo sự kiện ở quá khứ");
        }
    }

    // Tạo event mới
    public CalendarEventDTO createEvent(CalendarEventDTO eventRequest, String creatorId) {
        validateEventTime(eventRequest);

        UserEntity creator = userRepository.findById(UUID.fromString(creatorId))
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        CalendarEventEntity calendarEvent = new CalendarEventEntity();
        mapDtoToEntity(eventRequest, calendarEvent);
        calendarEvent.setCreatedBy(creator);
        calendarEvent.setSpace(spaceRepository.getReferenceById(UUID.fromString(eventRequest.getSpaceId())));

        CalendarEventEntity savedEvent = calendarEventRepository.save(calendarEvent);
        CalendarEventDTO result = new CalendarEventDTO(savedEvent);
        broadcastCalendarUpdate(eventRequest.getSpaceId(), "CREATED", result);
        return result;
    }

    private void mapDtoToEntity(CalendarEventDTO source, CalendarEventEntity target) {
        target.setTitle(source.getTitle());
        target.setDescription(source.getDescription());
        target.setEventDate(source.getEventDate());
        target.setStartTime(source.getStartTime());
        target.setEndTime(source.getEndTime());
        target.setRecurrenceType(source.getRecurrenceType() != null ? source.getRecurrenceType() : RECURRENCE_NONE);
        target.setRecurrenceEndDate(source.getRecurrenceEndDate());
        target.setAllowEditAll(source.isAllowEditAll());
    }

    // Cập nhật event (kiểm tra quyền: creator hoặc allowEditAll)
    public CalendarEventDTO updateEvent(UUID eventId, CalendarEventDTO eventRequest, String userId) {
        CalendarEventEntity calendarEvent = calendarEventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event not found"));

        if (!hasPermissionToEdit(calendarEvent, UUID.fromString(userId))) {
            throw new SecurityException("Bạn không có quyền chỉnh sửa sự kiện này");
        }

        mapDtoToEntity(eventRequest, calendarEvent);

        CalendarEventEntity savedEvent = calendarEventRepository.save(calendarEvent);
        CalendarEventDTO result = new CalendarEventDTO(savedEvent);
        broadcastCalendarUpdate(result.getSpaceId(), "UPDATED", result);
        return result;
    }

    private boolean hasPermissionToEdit(CalendarEventEntity event, UUID userId) {
        return event.getCreatedBy().getId().equals(userId) || event.isAllowEditAll();
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
        // Kiểm tra xung đột bằng cách expand toàn bộ sự kiện của ngày đó (bao gồm cả các bản ghi lặp)
        List<CalendarEventDTO> dayEvents = getEventsByDateRange(spaceId, date, date);

        return dayEvents.stream()
                .filter(e -> excludeEventId == null || !e.getId().equals(excludeEventId))
                .filter(e -> e.getStartTime().isBefore(endTime) && e.getEndTime().isAfter(startTime))
                .collect(Collectors.toList());
    }
}
