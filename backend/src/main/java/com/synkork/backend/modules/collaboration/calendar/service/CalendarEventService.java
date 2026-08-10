package com.synkork.backend.modules.collaboration.calendar.service;

import com.synkork.backend.modules.collaboration.calendar.entity.CalendarEventEntity;
import com.synkork.backend.modules.collaboration.calendar.entity.EventAttachmentEntity;
import com.synkork.backend.modules.collaboration.calendar.enums.AttachmentTypeEnum;
import com.synkork.backend.modules.collaboration.calendar.dto.CalendarEventDTO;
import com.synkork.backend.modules.collaboration.calendar.dto.CalendarEventAttachmentDTO;
import com.synkork.backend.modules.collaboration.calendar.repository.CalendarEventRepository;
import com.synkork.backend.modules.roomMember.RoomMemberEntity;
import com.synkork.backend.modules.roomMember.RoomMemberRepository;
import com.synkork.backend.modules.space.SpaceEntity;
import com.synkork.backend.modules.space.SpaceRepository;
import com.synkork.backend.modules.space.SpaceService;
import com.synkork.backend.modules.user.UserEntity;
import com.synkork.backend.modules.user.UserRepository;
import com.synkork.backend.modules.collaboration.task.card.CardRepository;
import com.synkork.backend.modules.collaboration.task.card.CardEntity;
import com.synkork.backend.modules.collaboration.note.NoteRepository;
import com.synkork.backend.modules.collaboration.note.NoteEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import org.springframework.web.multipart.MultipartFile;
import com.synkork.backend.common.utils.FileService;
import com.synkork.backend.common.dtos.FileUploaded;

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
    private CardRepository cardRepository;

    @Autowired
    private NoteRepository noteRepository;

    @Autowired
    private SpaceRepository spaceRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoomMemberRepository roomMemberRepository;

    @Autowired
    private com.synkork.backend.common.utils.LLMFunction.TikaFileService tikaFileService;

    @Autowired
    private SpaceService spaceService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private GoogleCalendarService googleCalendarService;

    @Autowired
    private FileService fileService;

    @Autowired
    private CalendarEmailService calendarEmailService;

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
        if (start == null || end == null) {
            throw new IllegalArgumentException("Start date and end date must not be null");
        }
        if (end.isBefore(start)) {
            throw new IllegalArgumentException("Start date must be before or equal to end date");
        }
        if (ChronoUnit.YEARS.between(start, end) > 1) {
            throw new IllegalArgumentException("Khoảng thời gian tìm kiếm không được vượt quá 1 năm.");
        }
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
        LocalDate eventStart = event.getEventDate();
        LocalDate eventEnd = event.getEndDate() != null ? event.getEndDate() : eventStart;
        if (!eventEnd.isBefore(start) && !eventStart.isAfter(end)) {
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

        LocalDate origStart = original.getEventDate();
        LocalDate origEnd = original.getEndDate() != null ? original.getEndDate() : origStart;
        long durationDays = ChronoUnit.DAYS.between(origStart, origEnd);
        if (durationDays < 0) durationDays = 0;

        instance.setEndDate(date.plusDays(durationDays));
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

    private void validateEventRequest(CalendarEventDTO request) {
        if (request == null) {
            throw new IllegalArgumentException("Thông tin sự kiện không được null");
        }
        if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Tiêu đề sự kiện không được để trống.");
        }
        if (request.getTitle().trim().length() > 255) {
            throw new IllegalArgumentException("Tiêu đề sự kiện không được vượt quá 255 ký tự.");
        }
        if (request.getDescription() != null && request.getDescription().length() > 2000) {
            throw new IllegalArgumentException("Mô tả sự kiện không được vượt quá 2000 ký tự.");
        }
        if (request.getEventDate() == null) {
            throw new IllegalArgumentException("Ngày diễn ra sự kiện không được để trống.");
        }
        if (request.getStartTime() == null) {
            throw new IllegalArgumentException("Thời gian bắt đầu không được để trống.");
        }
        LocalDate endDate = request.getEndDate() != null ? request.getEndDate() : request.getEventDate();
        boolean isOvernight = request.getEndTime().isBefore(request.getStartTime());
        if (isOvernight && endDate.equals(request.getEventDate())) {
            endDate = request.getEventDate().plusDays(1);
        }
        java.time.LocalDateTime startDateTime = java.time.LocalDateTime.of(request.getEventDate(), request.getStartTime());
        java.time.LocalDateTime endDateTime = java.time.LocalDateTime.of(endDate, request.getEndTime());

        if (!endDateTime.isAfter(startDateTime)) {
            throw new IllegalArgumentException("Thời gian kết thúc phải sau thời gian bắt đầu.");
        }
    }

    // Tạo event mới
    @Transactional
    public CalendarEventDTO createEvent(CalendarEventDTO eventRequest, UUID creatorId) {
        validateEventRequest(eventRequest);

        UUID spaceId = UUID.fromString(eventRequest.getSpaceId());
        if (!spaceService.checkUserAccess(spaceId, creatorId)) {
            throw new SecurityException("Bạn không có quyền truy cập vào không gian này.");
        }

        UserEntity creator = userRepository.findById(creatorId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng với ID: " + creatorId));

        SpaceEntity space = spaceRepository.findById(spaceId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy không gian với ID: " + spaceId));

        // Kiểm tra tạo chuỗi sự kiện liên tục theo ngày (Lịch liên tục Schedule)
        LocalDate endDate = eventRequest.getEndDate();
        boolean isMultiDaySchedule = endDate != null && endDate.isAfter(eventRequest.getEventDate());

        if (isMultiDaySchedule) {
            return createScheduleEvents(eventRequest, creator, space);
        }

        // Xử lý sự kiện thông thường (1 ngày hoặc 1 ca qua đêm)
        CalendarEventEntity calendarEvent = new CalendarEventEntity();
        eventRequest.updateEntity(calendarEvent);
        calendarEvent.setCreatedBy(creator);
        calendarEvent.setSpace(space);
        calendarEvent.setSchedule(false);
        calendarEvent.setScheduleId(null);
        
        boolean isOvernight = calendarEvent.getEndTime().isBefore(calendarEvent.getStartTime());
        if (calendarEvent.getEndDate() == null || calendarEvent.getEndDate().equals(calendarEvent.getEventDate())) {
            calendarEvent.setEndDate(isOvernight ? calendarEvent.getEventDate().plusDays(1) : calendarEvent.getEventDate());
        }
        applyRelations(calendarEvent, eventRequest);
        syncEventRelations(calendarEvent, eventRequest, creator);

        CalendarEventEntity savedEvent = calendarEventRepository.save(Objects.requireNonNull(calendarEvent));
        googleCalendarService.syncEventToGoogleAsync(savedEvent.getId());
        
        CalendarEventDTO result = new CalendarEventDTO(savedEvent);
        broadcastCalendarUpdate(eventRequest.getSpaceId(), "CREATED", result);

        if (!savedEvent.getAttendees().isEmpty()) {
            calendarEmailService.sendEventNotificationEmail(savedEvent, savedEvent.getAttendees(), false);
        }
        
        return result;
    }

    // Tạo danh sách các sự kiện liên tục cho từng ngày (Lịch liên tục Schedule)
    private CalendarEventDTO createScheduleEvents(CalendarEventDTO eventRequest, UserEntity creator, SpaceEntity space) {
        UUID groupId = UUID.randomUUID();
        LocalDate current = eventRequest.getEventDate();
        LocalDate endDate = eventRequest.getEndDate();
        List<CalendarEventEntity> savedEvents = new ArrayList<>();

        boolean isOvernight = eventRequest.getEndTime().isBefore(eventRequest.getStartTime());

        while (!current.isAfter(endDate)) {
            CalendarEventEntity instance = new CalendarEventEntity();
            eventRequest.updateEntity(instance);
            instance.setEventDate(current);
            // Nếu là ca qua đêm, ngày kết thúc của ca là ngày tiếp theo
            instance.setEndDate(isOvernight ? current.plusDays(1) : current);
            instance.setCreatedBy(creator);
            instance.setSpace(space);
            instance.setSchedule(true);
            instance.setScheduleId(groupId);
            applyRelations(instance, eventRequest);
            syncEventRelations(instance, eventRequest, creator);

            CalendarEventEntity saved = calendarEventRepository.save(instance);
            googleCalendarService.syncEventToGoogleAsync(saved.getId());
            savedEvents.add(saved);
            current = current.plusDays(1);
        }

        // Phát sóng cập nhật qua WebSocket và gửi email thông báo
        for (CalendarEventEntity saved : savedEvents) {
            CalendarEventDTO dto = new CalendarEventDTO(saved);
            broadcastCalendarUpdate(eventRequest.getSpaceId(), "CREATED", dto);
        }
        
        if (!savedEvents.isEmpty() && !savedEvents.get(0).getAttendees().isEmpty()) {
            calendarEmailService.sendEventNotificationEmail(savedEvents.get(0), savedEvents.get(0).getAttendees(), false);
        }
        return new CalendarEventDTO(savedEvents.get(0));
    }

    // Cập nhật sự kiện (kiểm tra quyền: người tạo hoặc cho phép mọi người sửa)
    @Transactional
    public CalendarEventDTO updateEvent(UUID eventId, CalendarEventDTO eventRequest, UUID userId) {
        validateEventRequest(eventRequest);

        CalendarEventEntity calendarEvent = calendarEventRepository.findById(Objects.requireNonNull(eventId))
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sự kiện"));

        // Optimistic Locking: reject stale version
        if (eventRequest.getVersion() != null && calendarEvent.getVersion() != null
                && !calendarEvent.getVersion().equals(eventRequest.getVersion())) {
            throw new ObjectOptimisticLockingFailureException(CalendarEventEntity.class, eventId);
        }

        if (!hasPermissionToEdit(calendarEvent, userId)) {
            throw new SecurityException("Bạn không có quyền chỉnh sửa sự kiện này! Vui lòng liên hệ đến người tạo sự kiện");
        }

        UserEntity actor = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng với ID: " + userId));

        // Logic cập nhật nhóm sự kiện liên tục
        // Nếu sự kiện thuộc nhóm liên tục và ngày bắt đầu thay đổi -> Tạo lại toàn bộ nhóm sự kiện
        if (calendarEvent.isSchedule() && calendarEvent.getScheduleId() != null
                && !calendarEvent.getEventDate().equals(eventRequest.getEventDate())) {
            return regenerateScheduleGroup(calendarEvent, eventRequest, actor);
        }

        // Nếu sự kiện thuộc nhóm liên tục nhưng chỉ thay đổi giờ/nội dung -> Cập nhật toàn bộ sự kiện trong nhóm
        if (calendarEvent.isSchedule() && calendarEvent.getScheduleId() != null) {
            return updateScheduleGroupTime(calendarEvent, eventRequest, actor);
        }

        // Kiểm tra nếu sự kiện đơn chuyển thành sự kiện kéo dài nhiều ngày
        LocalDate endDate = eventRequest.getEndDate();
        boolean becomingMultiDay = endDate != null && endDate.isAfter(eventRequest.getEventDate());
        if (becomingMultiDay) {
            // Xóa sự kiện đơn cũ và tạo nhóm sự kiện liên tục mới
            String spaceIdStr = calendarEvent.getSpace().getId().toString();
            googleCalendarService.deleteEventFromGoogle(calendarEvent);
            
            // Broadcast DELETED for the old single event
            CalendarEventDTO deletedDto = new CalendarEventDTO(calendarEvent);
            broadcastCalendarUpdate(spaceIdStr, "DELETED", deletedDto);

            calendarEventRepository.delete(calendarEvent);
            calendarEventRepository.flush();
            return createScheduleEvents(eventRequest, actor, calendarEvent.getSpace());
        }

        // Sự kiện đơn lẻ thông thường
        List<RoomMemberEntity> oldAttendees = new ArrayList<>(calendarEvent.getAttendees());
        eventRequest.updateEntity(calendarEvent);
        if (calendarEvent.getEndDate() == null) {
            calendarEvent.setEndDate(calendarEvent.getEventDate());
        }
        applyRelations(calendarEvent, eventRequest);
        syncEventRelations(calendarEvent, eventRequest, actor);
        
        List<RoomMemberEntity> addedAttendees = calendarEvent.getAttendees().stream()
                .filter(a -> !oldAttendees.contains(a)).toList();
        
        CalendarEventEntity savedEvent = calendarEventRepository.save(Objects.requireNonNull(calendarEvent));
        googleCalendarService.syncEventToGoogleAsync(savedEvent.getId());
        
        CalendarEventDTO result = new CalendarEventDTO(savedEvent);
        broadcastCalendarUpdate(result.getSpaceId(), "UPDATED", result);

        if (!addedAttendees.isEmpty()) {
            calendarEmailService.sendEventNotificationEmail(savedEvent, addedAttendees, false);
        }
        
        return result;
    }

    // Tạo lại toàn bộ nhóm sự kiện liên tục khi thay đổi ngày
    private CalendarEventDTO regenerateScheduleGroup(CalendarEventEntity existing, CalendarEventDTO request, UserEntity actor) {
        UUID scheduleId = existing.getScheduleId();
        SpaceEntity space = existing.getSpace();
        String spaceIdStr = space.getId().toString();

        // Xóa tất cả sự kiện cũ trong nhóm
        List<CalendarEventEntity> oldGroup = calendarEventRepository.findByScheduleId(scheduleId);
        for (CalendarEventEntity old : oldGroup) {
            googleCalendarService.deleteEventFromGoogle(old);
            CalendarEventDTO dto = new CalendarEventDTO(old);
            broadcastCalendarUpdate(spaceIdStr, "DELETED", dto);
        }
        calendarEventRepository.deleteByScheduleId(scheduleId);
        calendarEventRepository.flush();

        // Kiểm tra sau khi chỉnh sửa có còn là sự kiện nhiều ngày không
        LocalDate endDate = request.getEndDate();
        boolean stillMultiDay = endDate != null && endDate.isAfter(request.getEventDate());

        if (stillMultiDay) {
            return createScheduleEvents(request, actor, space);
        } else {
            // Thu hẹp về sự kiện trong 1 ngày
            request.setSchedule(false);
            request.setScheduleId(null);
            CalendarEventEntity single = new CalendarEventEntity();
            request.updateEntity(single);
            single.setCreatedBy(actor);
            single.setSpace(space);
            if (single.getEndDate() == null) {
                single.setEndDate(single.getEventDate());
            }
            applyRelations(single, request);
            syncEventRelations(single, request, actor);

            CalendarEventEntity saved = calendarEventRepository.save(single);
            googleCalendarService.syncEventToGoogleAsync(saved.getId());
            CalendarEventDTO result = new CalendarEventDTO(saved);
            broadcastCalendarUpdate(spaceIdStr, "UPDATED", result);
            return result;
        }
    }

    // Cập nhật giờ/nội dung cho tất cả sự kiện trong nhóm (không đổi ngày diễn ra của từng ô)
    private CalendarEventDTO updateScheduleGroupTime(CalendarEventEntity triggering, CalendarEventDTO request, UserEntity actor) {
        UUID scheduleId = triggering.getScheduleId();
        List<CalendarEventEntity> group = calendarEventRepository.findByScheduleId(scheduleId);
        List<CalendarEventEntity> savedEvents = new ArrayList<>();

        for (CalendarEventEntity member : group) {
            LocalDate originalDate = member.getEventDate(); // Giữ nguyên ngày diễn ra của từng ô sự kiện
            List<RoomMemberEntity> oldAttendees = new ArrayList<>(member.getAttendees());
            request.updateEntity(member);
            member.setEventDate(originalDate); // Khôi phục lại ngày ban đầu của ô
            boolean isOvernight = member.getEndTime().isBefore(member.getStartTime());
            member.setEndDate(isOvernight ? originalDate.plusDays(1) : originalDate);
            member.setSchedule(true);
            member.setScheduleId(scheduleId);
            applyRelations(member, request);
            syncEventRelations(member, request, actor);
            CalendarEventEntity saved = calendarEventRepository.save(member);
            googleCalendarService.syncEventToGoogleAsync(saved.getId());
            savedEvents.add(saved);
        }

        for (CalendarEventEntity saved : savedEvents) {
            CalendarEventDTO dto = new CalendarEventDTO(saved);
            broadcastCalendarUpdate(dto.getSpaceId(), "UPDATED", dto);
        }
        
        return new CalendarEventDTO(savedEvents.get(0));
    }

    // Thiết lập mối liên kết (phòng họp, công việc, ghi chú)
    private void applyRelations(CalendarEventEntity calendarEvent, CalendarEventDTO eventRequest) {
        if (eventRequest.getCallRoomSpaceId() != null && !eventRequest.getCallRoomSpaceId().isEmpty()) {
            UUID callRoomSpaceId = UUID.fromString(eventRequest.getCallRoomSpaceId());
            SpaceEntity callRoomSpace = spaceRepository.findById(callRoomSpaceId)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phòng họp/không gian với ID: " + callRoomSpaceId));
            calendarEvent.setCallRoomSpace(callRoomSpace);
        } else {
            calendarEvent.setCallRoomSpace(null);
        }
        if (eventRequest.getTaskId() != null && !eventRequest.getTaskId().isEmpty()) {
            UUID taskId = UUID.fromString(eventRequest.getTaskId());
            CardEntity task = cardRepository.findById(taskId)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy task với ID: " + taskId));
            calendarEvent.setTask(task);
        } else {
            calendarEvent.setTask(null);
        }
        if (eventRequest.getNoteId() != null && !eventRequest.getNoteId().isEmpty()) {
            UUID noteId = UUID.fromString(eventRequest.getNoteId());
            NoteEntity note = noteRepository.findById(noteId)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy note với ID: " + noteId));
            calendarEvent.setNote(note);
        } else {
            calendarEvent.setNote(null);
        }
    }

    private boolean hasPermissionToEdit(CalendarEventEntity event, UUID userId) {
        boolean isMemberOfSpace = spaceService.checkUserAccess(event.getSpace().getId(), userId);
        if (!isMemberOfSpace) {
            return false;
        }
        boolean isCreator = event.getCreatedBy().getId().equals(userId);
        return isCreator || event.isAllowEditAll();
    }

    private void syncEventRelations(CalendarEventEntity event, CalendarEventDTO request, UserEntity actor) {
        event.replaceAttendees(buildAttendees(event, request.getAttendeeIds()));
        event.replaceAttachments(buildAttachments(event, request.getAttachments(), actor));
    }

    private List<RoomMemberEntity> buildAttendees(CalendarEventEntity event, List<String> attendeeIds) {
        if (attendeeIds == null || attendeeIds.isEmpty()) {
            return new ArrayList<>();
        }

        UUID roomId = event.getSpace().getRoom().getId();
        List<RoomMemberEntity> attendees = new ArrayList<>();
        Set<String> uniqueIds = new LinkedHashSet<>();

        for (String idStr : attendeeIds) {
            if (idStr == null || idStr.trim().isEmpty()) {
                continue;
            }

            String idTrim = idStr.trim();
            if (!uniqueIds.add(idTrim)) {
                continue;
            }

            UUID memberId = UUID.fromString(idTrim);
            RoomMemberEntity attendee = roomMemberRepository.findByRoom_IdAndId(roomId, memberId)
                    .orElseThrow(() -> new IllegalArgumentException("Khong tim thay thanh vien phong voi ID: " + memberId));
            attendees.add(attendee);
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
        return (size == null || size <= 0) ? 0 : size;
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

    // Xóa sự kiện (chỉ dành cho người tạo, hỗ trợ xóa toàn bộ nhóm sự kiện liên tục)
    @Transactional
    public void deleteEvent(UUID eventId, UUID userId) {
        CalendarEventEntity entity = calendarEventRepository.findById(Objects.requireNonNull(eventId))
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sự kiện"));

        boolean isCreator = entity.getCreatedBy().getId().equals(userId);
        boolean isMemberOfSpace = spaceService.checkUserAccess(entity.getSpace().getId(), userId);
        if (!isCreator || !isMemberOfSpace) {
            throw new SecurityException("Bạn không có quyền xóa sự kiện này");
        }

        String spaceIdStr = entity.getSpace().getId().toString();

        // Nếu là sự kiện liên tục, xóa toàn bộ các sự kiện trong nhóm
        if (entity.isSchedule() && entity.getScheduleId() != null) {
            List<CalendarEventEntity> group = calendarEventRepository.findByScheduleId(entity.getScheduleId());
            for (CalendarEventEntity member : group) {
                CalendarEventDTO dto = new CalendarEventDTO(member);
                googleCalendarService.deleteEventFromGoogleAsync(member);
                broadcastCalendarUpdate(spaceIdStr, "DELETED", dto);
            }
            calendarEventRepository.deleteByScheduleId(entity.getScheduleId());
        } else {
            CalendarEventDTO deletedDto = new CalendarEventDTO(entity);
            googleCalendarService.deleteEventFromGoogleAsync(entity);
            calendarEventRepository.delete(entity);
            broadcastCalendarUpdate(spaceIdStr, "DELETED", deletedDto);
        }
    }


    @Transactional
    public List<CalendarEventAttachmentDTO> uploadAttachments(UUID eventId, List<MultipartFile> files, UUID userId) {
        CalendarEventEntity event = calendarEventRepository.findById(Objects.requireNonNull(eventId))
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sự kiện"));
        if (!hasPermissionToEdit(event, userId)) {
            throw new SecurityException("Không có quyền chỉnh sửa");
        }
        UserEntity uploader = userRepository.findById(userId).orElseThrow();
        
        List<EventAttachmentEntity> newAttachments = new ArrayList<>();
        
        for (MultipartFile file : files) {
            // Use FileService
            boolean isImage = file.getContentType() != null && file.getContentType().startsWith("image/");
            FileUploaded uploaded = fileService.handleUpload(file, "calendar_events");
                    
            EventAttachmentEntity attachment = new EventAttachmentEntity();
            attachment.setEvent(event);
            attachment.setUploadedBy(uploader);
            attachment.setFileName(uploaded.originalName());
            attachment.setFileUrl(uploaded.url());
            attachment.setFilePublicId(uploaded.publicId());
            attachment.setResourceType(uploaded.resourceType());
            attachment.setFileSizeKb((int)(file.getSize() / 1024));
            
            attachment.setType(isImage ? AttachmentTypeEnum.IMAGE : AttachmentTypeEnum.FILE);
            
            event.getAttachments().add(attachment);
            newAttachments.add(attachment);
        }
        
        calendarEventRepository.save(event);
        
        CalendarEventDTO result = new CalendarEventDTO(event);
        broadcastCalendarUpdate(event.getSpace().getId().toString(), "UPDATED", result);
        
        // Gửi mail thông báo đính kèm file sau khi upload hoàn tất
        calendarEmailService.sendEventNotificationEmail(event, event.getAttendees(), false);
        
        List<CalendarEventAttachmentDTO> resultList = new ArrayList<>();
        for (EventAttachmentEntity att : newAttachments) {
            resultList.add(new CalendarEventAttachmentDTO(att));
        }
        return resultList;
    }


    @Transactional(readOnly = true)
    public String summarizeAttachment(UUID eventId, UUID attachmentId, UUID userId) {
        CalendarEventEntity event = calendarEventRepository.findById(Objects.requireNonNull(eventId))
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sự kiện"));
                
        EventAttachmentEntity target = event.getAttachments().stream()
                .filter(a -> a.getId() != null && a.getId().equals(attachmentId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đính kèm"));
                
        return tikaFileService.convertClondinaryToString(target.getFileUrl());
    }

    @Transactional
    public void deleteAttachment(UUID eventId, UUID attachmentId, UUID userId) {
        CalendarEventEntity event = calendarEventRepository.findById(Objects.requireNonNull(eventId))
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sự kiện"));
        if (!hasPermissionToEdit(event, userId)) {
            throw new SecurityException("Không có quyền chỉnh sửa");
        }
        
        Optional<EventAttachmentEntity> target = event.getAttachments().stream()
                .filter(a -> a.getId() != null && a.getId().equals(attachmentId))
                .findFirst();
                
        if (target.isPresent()) {
            EventAttachmentEntity attachment = target.get();
            event.getAttachments().remove(attachment);
            calendarEventRepository.save(event);
            
            if (attachment.getFilePublicId() != null && attachment.getResourceType() != null) {
                fileService.deleteFile(attachment.getFilePublicId(), attachment.getResourceType());
            }
            
            CalendarEventDTO result = new CalendarEventDTO(event);
            broadcastCalendarUpdate(event.getSpace().getId().toString(), "UPDATED", result);
        }
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
