package com.synkork.backend.modules.collaboration.calendar.service;

import com.synkork.backend.modules.collaboration.calendar.entity.CalendarEventEntity;
import com.synkork.backend.modules.collaboration.calendar.entity.EventAttachmentEntity;
import com.synkork.backend.modules.collaboration.calendar.entity.EventAttendeeEntity;
import com.synkork.backend.modules.collaboration.calendar.enums.AttachmentTypeEnum;
import com.synkork.backend.modules.collaboration.calendar.dto.CalendarEventDTO;
import com.synkork.backend.modules.collaboration.calendar.dto.CalendarEventAttachmentDTO;
import com.synkork.backend.modules.collaboration.calendar.repository.CalendarEventRepository;
import com.synkork.backend.modules.space.SpaceRepository;
import com.synkork.backend.modules.user.UserEntity;
import com.synkork.backend.modules.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

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

    private void broadcastCalendarUpdate(String spaceId, String action, CalendarEventDTO event) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("action", action);
        payload.put("event", event);
        messagingTemplate.convertAndSend("/topic/space/" + spaceId + "/calendar", payload);
    }

    // Lấy tất cả event theo spaceId
    public List<CalendarEventDTO> getEventsBySpaceId(UUID spaceId) {
        List<CalendarEventEntity> events = calendarEventRepository.findBySpaceId(spaceId);
        List<CalendarEventDTO> result = new ArrayList<>();
        for (CalendarEventEntity event : events) {
            result.add(new CalendarEventDTO(event));
        }
        return result;
    }

    // Danh sách sự kiện trong khoảng thời gian
    public List<CalendarEventDTO> getEventsByDateRange(UUID spaceId, LocalDate start, LocalDate end) {
        List<CalendarEventEntity> targetEvents = calendarEventRepository
                .findBySpaceIdAndEventDateLessThanEqual(Objects.requireNonNull(spaceId, "SpaceID null"), end);
        List<CalendarEventDTO> expandedResults = new ArrayList<>();

        for (CalendarEventEntity event : targetEvents) {
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
        List<CalendarEventDTO> instances = new ArrayList<>();
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

    // Lấy sự kiện theo ngày cụ thể (bao gồm lịch lặp)
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
    @Transactional
    public CalendarEventDTO createEvent(CalendarEventDTO eventRequest, UUID creatorId) {
        // validateEventTime(eventRequest); // Bỏ comment để cho phép tạo sự kiện ở quá khứ

        // Tận dụng hàm có sẵn getReferenceById để lấy trực tiếp Proxy mà không cần Select DB
        UserEntity creator = userRepository.getReferenceById(creatorId);

        CalendarEventEntity calendarEvent = new CalendarEventEntity();
        eventRequest.updateEntity(calendarEvent);
        calendarEvent.setCreatedBy(creator);
        calendarEvent.setSpace(
                spaceRepository.getReferenceById(Objects.requireNonNull(UUID.fromString(eventRequest.getSpaceId()))));
        syncEventRelations(calendarEvent, eventRequest, creator);

        CalendarEventEntity savedEvent = calendarEventRepository.save(Objects.requireNonNull(calendarEvent));
        CalendarEventDTO result = new CalendarEventDTO(savedEvent);
        broadcastCalendarUpdate(eventRequest.getSpaceId(), "CREATED", result);
        return result;
    }

    // Cập nhật event (kiểm tra quyền: creator hoặc allowEditAll)
    @Transactional
    public CalendarEventDTO updateEvent(UUID eventId, CalendarEventDTO eventRequest, UUID userId) {
        // Null check cho IDE
        CalendarEventEntity calendarEvent = calendarEventRepository.findById(Objects.requireNonNull(eventId))
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sự kiện"));

        if (!hasPermissionToEdit(calendarEvent, userId)) {
            throw new SecurityException("Bạn không có quyền chỉnh sửa sự kiện này! Vui lòng liên hệ đến người tạo sự kiện");
        }
        eventRequest.updateEntity(calendarEvent);
        UserEntity actor = userRepository.getReferenceById(userId);
        syncEventRelations(calendarEvent, eventRequest, actor);
        CalendarEventEntity savedEvent = calendarEventRepository.save(Objects.requireNonNull(calendarEvent));
        CalendarEventDTO result = new CalendarEventDTO(savedEvent);
        broadcastCalendarUpdate(result.getSpaceId(), "UPDATED", result);
        return result;
    }

    private boolean hasPermissionToEdit(CalendarEventEntity event, UUID userId) {
        return event.getCreatedBy().getId().equals(userId) || event.isAllowEditAll();
    }

    private void syncEventRelations(CalendarEventEntity event, CalendarEventDTO request, UserEntity actor) {
        event.replaceAttendees(buildAttendees(event, request.getAttendees()));
        event.replaceAttachments(buildAttachments(event, request.getAttachments(), actor));
    }

    private List<EventAttendeeEntity> buildAttendees(CalendarEventEntity event, List<String> attendeeEmails) {
        if (attendeeEmails == null || attendeeEmails.isEmpty()) {
            return new ArrayList<>();
        }

        List<EventAttendeeEntity> attendees = new ArrayList<>();
        List<String> missingEmails = new ArrayList<>();
        Set<String> uniqueEmails = new LinkedHashSet<>();

        for (String rawEmail : attendeeEmails) {
            if (rawEmail == null) {
                continue;
            }

            String email = rawEmail.trim().toLowerCase();
            if (email.isEmpty() || !uniqueEmails.add(email)) {
                continue;
            }

            Optional<UserEntity> matchedUser = userRepository.findByEmail(email);
            if (matchedUser.isEmpty()) {
                missingEmails.add(email);
                continue;
            }

            EventAttendeeEntity attendee = new EventAttendeeEntity();
            attendee.setEvent(event);
            attendee.setUser(matchedUser.get());
            attendees.add(attendee);
        }

        if (!missingEmails.isEmpty()) {
            throw new IllegalArgumentException("Không tìm thấy người dùng cho các email: " + String.join(", ", missingEmails));
        }

        return attendees;
    }

    private List<EventAttachmentEntity> buildAttachments(
            CalendarEventEntity event,
            List<CalendarEventAttachmentDTO> requestAttachments,
            UserEntity actor
    ) {
        if (requestAttachments == null || requestAttachments.isEmpty()) {
            return new ArrayList<>();
        }

        List<EventAttachmentEntity> attachments = new ArrayList<>();

        for (CalendarEventAttachmentDTO requestAttachment : requestAttachments) {
            if (requestAttachment == null || isBlank(requestAttachment.getName())) {
                continue;
            }

            EventAttachmentEntity attachment = new EventAttachmentEntity();
            attachment.setEvent(event);
            attachment.setUploadedBy(actor);
            attachment.setFileName(requestAttachment.getName().trim());
            attachment.setFileUrl(normalizeAttachmentUrl(requestAttachment.getFileUrl()));
            attachment.setFileSizeKb(normalizeAttachmentSize(requestAttachment.getSize()));
            attachment.setType(resolveAttachmentType(requestAttachment));
            attachments.add(attachment);
        }

        return attachments;
    }

    private Integer normalizeAttachmentSize(Integer size) {
        if (size == null || size <= 0) {
            return 0;
        }
        return size;
    }

    private String normalizeAttachmentUrl(String fileUrl) {
        return fileUrl == null ? "" : fileUrl.trim();
    }

    private AttachmentTypeEnum resolveAttachmentType(CalendarEventAttachmentDTO attachment) {
        if (!isBlank(attachment.getType())) {
            try {
                return AttachmentTypeEnum.valueOf(attachment.getType().trim().toUpperCase());
            } catch (IllegalArgumentException ignored) {
            }
        }

        String fileName = attachment.getName().toLowerCase();
        boolean isImage = fileName.endsWith(".png")
                || fileName.endsWith(".jpg")
                || fileName.endsWith(".jpeg")
                || fileName.endsWith(".gif")
                || fileName.endsWith(".webp")
                || fileName.endsWith(".svg");

        return isImage ? AttachmentTypeEnum.IMAGE : AttachmentTypeEnum.FILE;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    // Xóa event (chỉ creator)
    @Transactional
    public void deleteEvent(UUID eventId, UUID userId) {
        // Null check cho IDE
        CalendarEventEntity entity = calendarEventRepository.findById(Objects.requireNonNull(eventId))
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sự kiện"));

        if (!entity.getCreatedBy().getId().equals(userId)) {
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
        // Liệt kê mọi sự kiện trong ngày (bao gồm sự kiện lặp) để tìm trùng lặp
        List<CalendarEventDTO> dayEvents = getEventsByDateRange(spaceId, date, date);
        List<CalendarEventDTO> conflicts = new ArrayList<>();

        for (CalendarEventDTO event : dayEvents) {
            if (excludeEventId != null && event.getId().equals(excludeEventId)) {
                continue;
            }
            if (event.getStartTime().isBefore(endTime) && event.getEndTime().isAfter(startTime)) {
                conflicts.add(event);
            }
        }

        return conflicts;
    }
}
