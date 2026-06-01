package com.synkork.backend.modules.collaboration.calendar.service;

import com.synkork.backend.common.dtos.FileUploaded;
import com.synkork.backend.common.utils.EmailService;
import com.synkork.backend.common.utils.FileService;
import com.synkork.backend.modules.collaboration.calendar.entity.CalendarEventEntity;
import com.synkork.backend.modules.collaboration.calendar.entity.EventAttachmentEntity;
import com.synkork.backend.modules.collaboration.calendar.entity.EventAttendeeEntity;
import com.synkork.backend.modules.collaboration.calendar.enums.AttachmentTypeEnum;
import com.synkork.backend.modules.collaboration.calendar.dto.CalendarEventDTO;
import com.synkork.backend.modules.collaboration.calendar.dto.CalendarEventAttachmentDTO;
import com.synkork.backend.modules.collaboration.calendar.repository.CalendarEventRepository;
import com.synkork.backend.modules.roomMember.RoomMemberRepository;
import com.synkork.backend.modules.space.SpaceRepository;
import com.synkork.backend.modules.user.UserEntity;
import com.synkork.backend.modules.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class CalendarEventService {

    private static final int DEFAULT_RECURRENCE_LIMIT_YEARS = 1;
    private static final String RECURRENCE_NONE = "NONE";
    private static final String RECURRENCE_DAILY = "DAILY";
    private static final String RECURRENCE_WEEKLY = "WEEKLY";
    private static final String RECURRENCE_MONTHLY = "MONTHLY";
    private static final String RECURRENCE_YEARLY = "YEARLY";

    private final CalendarEventRepository calendarEventRepository;

    private final SpaceRepository spaceRepository;

    private final RoomMemberRepository roomMemberRepository;

    private final UserRepository userRepository;

    private final FileService fileService;

    private final EmailService emailService;

    private final SimpMessagingTemplate messagingTemplate;

    private void broadcastCalendarUpdate(String spaceId, String action, CalendarEventDTO event) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("action", action);
        payload.put("event", event);
        messagingTemplate.convertAndSend("/topic/space/" + spaceId + "/calendar", payload);
    }

    public List<CalendarEventDTO> getEventsBySpaceId(UUID spaceId) {
        List<CalendarEventEntity> events = calendarEventRepository.findBySpaceId(spaceId);
        List<CalendarEventDTO> result = new ArrayList<>();
        for (CalendarEventEntity event : events) {
            LocalDate endDate = event.getEndDate() != null ? event.getEndDate() : event.getEventDate();
            result.addAll(createDisplaySegments(event, event.getEventDate(), endDate));
        }
        return result;
    }

    public List<CalendarEventDTO> getEventsByDateRange(UUID spaceId, LocalDate start, LocalDate end) {
        List<CalendarEventDTO> expandedResults = new ArrayList<>();

        for (CalendarEventEntity event : calendarEventRepository.findOverlappingDateRange(spaceId, start, end)) {
            if (isNonRecurring(event)) {
                expandedResults.addAll(createDisplaySegments(event, start, end));
            }
        }

        for (CalendarEventEntity event : calendarEventRepository.findRecurringBySpaceIdStartingBeforeOrOn(spaceId, end)) {
            expandedResults.addAll(expandRecurringEvent(event, start, end));
        }

        return expandedResults;
    }

    private boolean isNonRecurring(CalendarEventEntity event) {
        return RECURRENCE_NONE.equals(event.getRecurrenceType()) || event.getRecurrenceType() == null;
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
                instances.addAll(createDisplaySegments(
                        event,
                        currentOccurrence,
                        currentOccurrence.plusDays(calculateEventDurationInDays(event)),
                        rangeStart,
                        rangeEnd
                ));
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

    private long calculateEventDurationInDays(CalendarEventEntity event) {
        LocalDate endDate = event.getEndDate() != null ? event.getEndDate() : event.getEventDate();
        return java.time.temporal.ChronoUnit.DAYS.between(event.getEventDate(), endDate);
    }

    private List<CalendarEventDTO> createDisplaySegments(CalendarEventEntity event, LocalDate rangeStart, LocalDate rangeEnd) {
        LocalDate endDate = event.getEndDate() != null ? event.getEndDate() : event.getEventDate();
        return createDisplaySegments(event, event.getEventDate(), endDate, rangeStart, rangeEnd);
    }

    private List<CalendarEventDTO> createDisplaySegments(
            CalendarEventEntity event,
            LocalDate startDate,
            LocalDate endDate,
            LocalDate rangeStart,
            LocalDate rangeEnd
    ) {
        List<CalendarEventDTO> segments = new ArrayList<>();
        LocalDate segmentDate = startDate.isAfter(rangeStart) ? startDate : rangeStart;
        LocalDate lastDate = endDate.isBefore(rangeEnd) ? endDate : rangeEnd;

        while (!segmentDate.isAfter(lastDate)) {
            CalendarEventDTO segment = new CalendarEventDTO(event);
            segment.setEventDate(startDate);
            segment.setEndDate(endDate);
            segment.setDisplayDate(segmentDate);
            segment.setDisplayStartTime(segmentDate.equals(startDate) ? event.getStartTime() : LocalTime.MIDNIGHT);
            segment.setDisplayEndTime(segmentDate.equals(endDate) ? event.getEndTime() : LocalTime.of(23, 59));
            segment.setContinuesFromPreviousDay(segmentDate.isAfter(startDate));
            segment.setContinuesToNextDay(segmentDate.isBefore(endDate));
            segment.setOriginalStartDateTime(startDate + "T" + event.getStartTime());
            segment.setOriginalEndDateTime(endDate + "T" + event.getEndTime());
            segments.add(segment);
            segmentDate = segmentDate.plusDays(1);
        }

        return segments;
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

    public List<CalendarEventDTO> getEventsByDate(UUID spaceId, LocalDate date) {
        return getEventsByDateRange(spaceId, date, date);
    }

    private void validateEventTime(CalendarEventDTO eventRequest) {
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();
        LocalDate endDate = eventRequest.getEndDate() != null ? eventRequest.getEndDate() : eventRequest.getEventDate();
        LocalDateTime startsAt = LocalDateTime.of(eventRequest.getEventDate(), eventRequest.getStartTime());
        LocalDateTime endsAt = LocalDateTime.of(endDate, eventRequest.getEndTime());

        if (!endsAt.isAfter(startsAt)) {
            throw new IllegalArgumentException("Thời gian kết thúc phải sau thời gian bắt đầu");
        }

        if (eventRequest.getEventDate().isBefore(today)) {
            throw new IllegalArgumentException("Không thể tạo sự kiện ở quá khứ");
        }

        if (eventRequest.getEventDate().isEqual(today) && eventRequest.getStartTime().isBefore(now)) {
            throw new IllegalArgumentException("Không thể tạo sự kiện ở quá khứ");
        }
    }

    @Transactional
    public CalendarEventDTO createEvent(CalendarEventDTO eventRequest, UUID creatorId) {
        validateEventTime(eventRequest);

        UserEntity creator = userRepository.getReferenceById(creatorId);

        CalendarEventEntity calendarEvent = new CalendarEventEntity();
        eventRequest.updateEntity(calendarEvent);
        calendarEvent.setCreatedBy(creator);
        calendarEvent.setSpace(
                spaceRepository.getReferenceById(Objects.requireNonNull(UUID.fromString(eventRequest.getSpaceId()))));
        syncEventRelations(calendarEvent, eventRequest, creator);

        CalendarEventEntity savedEvent = calendarEventRepository.save(Objects.requireNonNull(calendarEvent));
        sendNewAttendeeEmails(savedEvent, savedEvent.getAttendees(), creator);
        CalendarEventDTO result = new CalendarEventDTO(savedEvent);
        broadcastCalendarUpdate(eventRequest.getSpaceId(), "CREATED", result);
        return result;
    }

    @Transactional
    public CalendarEventDTO updateEvent(UUID eventId, CalendarEventDTO eventRequest, UUID userId) {
        CalendarEventEntity calendarEvent = calendarEventRepository.findById(Objects.requireNonNull(eventId))
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sự kiện"));

        if (!hasPermissionToEdit(calendarEvent, userId)) {
            throw new SecurityException("Bạn không có quyền chỉnh sửa sự kiện này! Vui lòng liên hệ đến người tạo sự kiện");
        }
        validateEventTime(eventRequest);
        Set<UUID> previousAttendeeIds = getAttendeeUserIds(calendarEvent.getAttendees());
        eventRequest.updateEntity(calendarEvent);
        UserEntity actor = userRepository.getReferenceById(userId);
        syncEventRelations(calendarEvent, eventRequest, actor);
        CalendarEventEntity savedEvent = calendarEventRepository.save(Objects.requireNonNull(calendarEvent));
        sendNewAttendeeEmails(savedEvent, findNewAttendees(savedEvent.getAttendees(), previousAttendeeIds), actor);
        CalendarEventDTO result = new CalendarEventDTO(savedEvent);
        broadcastCalendarUpdate(result.getSpaceId(), "UPDATED", result);
        return result;
    }

    private boolean hasPermissionToEdit(CalendarEventEntity event, UUID userId) {
        return event.getCreatedBy().getId().equals(userId) || event.isAllowEditAll();
    }

    private void syncEventRelations(CalendarEventEntity event, CalendarEventDTO request, UserEntity actor) {
        event.replaceAttendees(buildAttendees(event, request.getAttendeeIds()));
        event.replaceAttachments(buildAttachments(event, request.getAttachments(), actor));
    }

    private List<EventAttendeeEntity> buildAttendees(CalendarEventEntity event, List<UUID> attendeeIds) {
        if (attendeeIds == null || attendeeIds.isEmpty()) {
            return new ArrayList<>();
        }

        List<EventAttendeeEntity> attendees = new ArrayList<>();
        List<UUID> missingMemberIds = new ArrayList<>();
        Set<UUID> uniqueIds = new LinkedHashSet<>();
        UUID roomId = event.getSpace().getRoom().getId();

        for (UUID attendeeId : attendeeIds) {
            if (attendeeId == null || !uniqueIds.add(attendeeId)) {
                continue;
            }

            Optional<UserEntity> matchedUser = roomMemberRepository.findByRoom_IdAndUser_Id(roomId, attendeeId)
                    .map(roomMember -> roomMember.getUser());
            if (matchedUser.isEmpty()) {
                missingMemberIds.add(attendeeId);
                continue;
            }

            EventAttendeeEntity attendee = new EventAttendeeEntity();
            attendee.setEvent(event);
            attendee.setUser(matchedUser.get());
            attendees.add(attendee);
        }

        if (!missingMemberIds.isEmpty()) {
            throw new IllegalArgumentException("Người tham gia không thuộc phòng hiện tại: " + missingMemberIds);
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
            attachment.setPublicId(normalizeAttachmentUrl(requestAttachment.getPublicId()));
            attachment.setResourceType(normalizeAttachmentUrl(requestAttachment.getResourceType()));
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

    @Transactional
    public void deleteEvent(UUID eventId, UUID userId) {
        CalendarEventEntity entity = calendarEventRepository.findById(Objects.requireNonNull(eventId))
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sự kiện"));

        if (!entity.getCreatedBy().getId().equals(userId)) {
            throw new SecurityException("Bạn không có quyền xóa sự kiện này");
        }

        CalendarEventDTO deletedDto = new CalendarEventDTO(entity);
        calendarEventRepository.delete(entity);
        broadcastCalendarUpdate(deletedDto.getSpaceId(), "DELETED", deletedDto);
    }

    public List<CalendarEventDTO> findConflicts(UUID spaceId, LocalDate date, LocalDate requestEndDate, LocalTime startTime, LocalTime endTime,
            UUID excludeEventId) {
        LocalDate endDate = requestEndDate != null ? requestEndDate : (endTime.isAfter(startTime) ? date : date.plusDays(1));
        LocalDateTime requestedStart = LocalDateTime.of(date, startTime);
        LocalDateTime requestedEnd = LocalDateTime.of(endDate, endTime);
        List<CalendarEventEntity> dayEvents = calendarEventRepository.findOverlappingDateRange(spaceId, date, endDate);
        List<CalendarEventDTO> conflicts = new ArrayList<>();

        for (CalendarEventEntity event : dayEvents) {
            if (excludeEventId != null && event.getId().equals(excludeEventId)) {
                continue;
            }
            if (eventsOverlap(requestedStart, requestedEnd, event)) {
                conflicts.addAll(createDisplaySegments(event, date, endDate));
            }
        }

        return conflicts;
    }

    @Transactional
    public CalendarEventDTO uploadAttachments(UUID eventId, List<MultipartFile> files, UUID userId) {
        CalendarEventEntity event = calendarEventRepository.findById(Objects.requireNonNull(eventId))
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sự kiện"));

        if (!hasPermissionToEdit(event, userId)) {
            throw new SecurityException("Bạn không có quyền chỉnh sửa sự kiện này! Vui lòng liên hệ đến người tạo sự kiện");
        }

        UserEntity actor = userRepository.getReferenceById(userId);
        List<EventAttachmentEntity> attachments = new ArrayList<>(event.getAttachments());
        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) {
                continue;
            }

            FileUploaded uploaded = isImage(file.getOriginalFilename())
                    ? fileService.uploadImage(file, "calendar_event/" + eventId)
                    : fileService.uploadFile(file, "calendar_event/" + eventId);

            EventAttachmentEntity attachment = new EventAttachmentEntity();
            attachment.setEvent(event);
            attachment.setUploadedBy(actor);
            attachment.setFileName(uploaded.originalName());
            attachment.setFileUrl(uploaded.url());
            attachment.setPublicId(uploaded.publicId());
            attachment.setResourceType(uploaded.resourceType());
            attachment.setFileSizeKb(Math.max(1, (int) Math.ceil(file.getSize() / 1024.0)));
            attachment.setType(isImage(file.getOriginalFilename()) ? AttachmentTypeEnum.IMAGE : AttachmentTypeEnum.FILE);
            attachments.add(attachment);
        }

        event.replaceAttachments(attachments);
        CalendarEventEntity savedEvent = calendarEventRepository.save(event);
        CalendarEventDTO result = new CalendarEventDTO(savedEvent);
        broadcastCalendarUpdate(result.getSpaceId(), "UPDATED", result);
        return result;
    }

    private boolean eventsOverlap(LocalDateTime requestedStart, LocalDateTime requestedEnd, CalendarEventEntity event) {
        LocalDate endDate = event.getEndDate() != null ? event.getEndDate() : event.getEventDate();
        LocalDateTime eventStart = LocalDateTime.of(event.getEventDate(), event.getStartTime());
        LocalDateTime eventEnd = LocalDateTime.of(endDate, event.getEndTime());
        return eventStart.isBefore(requestedEnd) && eventEnd.isAfter(requestedStart);
    }

    private boolean isImage(String fileName) {
        if (fileName == null) {
            return false;
        }
        String normalizedName = fileName.toLowerCase();
        return normalizedName.endsWith(".png")
                || normalizedName.endsWith(".jpg")
                || normalizedName.endsWith(".jpeg")
                || normalizedName.endsWith(".gif")
                || normalizedName.endsWith(".webp")
                || normalizedName.endsWith(".svg");
    }

    private Set<UUID> getAttendeeUserIds(List<EventAttendeeEntity> attendees) {
        Set<UUID> ids = new HashSet<>();
        if (attendees == null) {
            return ids;
        }
        for (EventAttendeeEntity attendee : attendees) {
            ids.add(attendee.getUser().getId());
        }
        return ids;
    }

    private List<EventAttendeeEntity> findNewAttendees(List<EventAttendeeEntity> attendees, Set<UUID> previousAttendeeIds) {
        List<EventAttendeeEntity> newAttendees = new ArrayList<>();
        if (attendees == null) {
            return newAttendees;
        }
        for (EventAttendeeEntity attendee : attendees) {
            if (!previousAttendeeIds.contains(attendee.getUser().getId())) {
                newAttendees.add(attendee);
            }
        }
        return newAttendees;
    }

    private void sendNewAttendeeEmails(CalendarEventEntity event, List<EventAttendeeEntity> attendees, UserEntity actor) {
        if (attendees == null || attendees.isEmpty()) {
            return;
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy");
        LocalDate endDate = event.getEndDate() != null ? event.getEndDate() : event.getEventDate();
        String startsAt = LocalDateTime.of(event.getEventDate(), event.getStartTime()).format(formatter);
        String endsAt = LocalDateTime.of(endDate, event.getEndTime()).format(formatter);
        String subject = "[Synkork] Bạn được thêm vào sự kiện";
        String body = """
                <div style="font-family: Arial, sans-serif; max-width: 560px; margin: 0 auto;">
                    <h2 style="color: #023c3d;">Bạn được thêm vào sự kiện</h2>
                    <p><b>%s</b> đã thêm bạn vào sự kiện <b>%s</b>.</p>
                    <p><b>Thời gian:</b> %s - %s</p>
                    <p style="color:#666;">%s</p>
                </div>
                """.formatted(
                actor.getDisplayName() != null ? actor.getDisplayName() : actor.getUsername(),
                event.getTitle(),
                startsAt,
                endsAt,
                event.getDescription() != null ? event.getDescription() : ""
        );

        for (EventAttendeeEntity attendee : attendees) {
            String email = attendee.getUser().getEmail();
            if (email != null && !email.isBlank()) {
                emailService.send(email, subject, body);
            }
        }
    }
}
