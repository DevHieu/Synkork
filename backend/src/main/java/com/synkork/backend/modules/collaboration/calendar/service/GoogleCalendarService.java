package com.synkork.backend.modules.collaboration.calendar.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.synkork.backend.modules.collaboration.calendar.dto.googleCalendar.GoogleCalendarEvent;
import com.synkork.backend.modules.collaboration.calendar.dto.googleCalendar.GoogleCalendarEventsResponse;
import com.synkork.backend.modules.collaboration.calendar.entity.CalendarEventEntity;
import com.synkork.backend.modules.collaboration.calendar.enums.SyncStatus;
import com.synkork.backend.modules.collaboration.calendar.repository.CalendarEventRepository;
import com.synkork.backend.modules.collaboration.calendar.utils.GoogleCalendarUtils;
import com.synkork.backend.modules.space.SpaceEntity;
import com.synkork.backend.modules.space.SpaceService;
import com.synkork.backend.modules.user.UserEntity;
import com.synkork.backend.modules.user.UserRepository;
import com.synkork.backend.modules.user.enums.PlanEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class GoogleCalendarService {

    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final List<String> SCOPES = Collections.singletonList(CalendarScopes.CALENDAR_EVENTS);
    private final SpaceService spaceService;
    private final GoogleCalendarUtils googleCalendarUtils;

    @Value("${google.client.id:${GOOGLE_CLIENT_ID:}}")
    private String clientId;

    @Value("${google.client.secret:${GOOGLE_CLIENT_SECRET:}}")
    private String clientSecret;

    private final UserRepository userRepository;
    private final CalendarEventRepository calendarEventRepository;
    private final RestClient restClient = RestClient.create();

    private Calendar getCalendarClient(UserEntity user) throws Exception {
        if (user == null || user.getCurrentPlan() != PlanEnum.BUSINESS || user.getGoogleCalendarAccessToken() == null) {
            return null;
        }

        // Auto refresh
        if (user.getGoogleCalendarAccessTokenExpiresAt() != null &&
                LocalDateTime.now().isAfter(user.getGoogleCalendarAccessTokenExpiresAt().minusMinutes(5))) {
            refreshToken(user);
        }

        GoogleCredential credential = new GoogleCredential.Builder()
                .setTransport(GoogleNetHttpTransport.newTrustedTransport())
                .setJsonFactory(JSON_FACTORY)
                .setClientSecrets(clientId, clientSecret)
                .build();
        credential.setAccessToken(user.getGoogleCalendarAccessToken());

        return new Calendar.Builder(GoogleNetHttpTransport.newTrustedTransport(), JSON_FACTORY, credential)
                .setApplicationName("Synkork")
                .build();
    }

    private void refreshToken(UserEntity user) {
        try {
            if (user == null || user.getCurrentPlan() != PlanEnum.BUSINESS || user.getGoogleCalendarRefreshToken() == null) return;

            GoogleCredential credential = new GoogleCredential.Builder()
                    .setTransport(GoogleNetHttpTransport.newTrustedTransport())
                    .setJsonFactory(JSON_FACTORY)
                    .setClientSecrets(clientId, clientSecret)
                    .build();
            credential.setRefreshToken(user.getGoogleCalendarRefreshToken());
            
            if (credential.refreshToken()) {
                user.setGoogleCalendarAccessToken(credential.getAccessToken());
                user.setGoogleCalendarAccessTokenExpiresAt(LocalDateTime.now().plusSeconds(credential.getExpiresInSeconds()));
                userRepository.save(user);
            }
        } catch (Exception e) {
            log.error("Failed to refresh token", e);
        }
    }

    public void syncEventToGoogleAsync(UUID eventId) {
        System.out.println("RUNNINGGG");
        CompletableFuture.runAsync(() -> syncEventToGoogle(eventId));
    }

    public void deleteEventFromGoogleAsync(CalendarEventEntity entity) {
        if (entity == null || entity.getGoogleEventId() == null) return;
        CompletableFuture.runAsync(() -> deleteEventFromGoogle(entity));
    }

    public void syncEventToGoogle(UUID eventId) {
        System.out.println("SYNCING");

        CalendarEventEntity entity = calendarEventRepository.findById(eventId).orElse(null);
        if (entity == null) return;

        UserEntity user = entity.getCreatedBy();
        if (user == null || user.getCurrentPlan() != PlanEnum.BUSINESS) {
            log.info("Bỏ qua đồng bộ Google Calendar cho người dùng không phải gói BUSINESS");
            return;
        }

        try {
            Calendar client = getCalendarClient(user);
            if (client == null) return;

            Event event = new Event()
                    .setSummary(entity.getTitle())
                    .setDescription(entity.getDescription());

            DateTime startDateTime = new DateTime(java.util.Date.from(entity.getEventDate().atTime(entity.getStartTime()).atZone(ZoneId.systemDefault()).toInstant()));
            EventDateTime start = new EventDateTime().setDateTime(startDateTime).setTimeZone(ZoneId.systemDefault().getId());
            event.setStart(start);

            LocalDate endDateVal = entity.getEndDate() != null ? entity.getEndDate() : entity.getEventDate();
            DateTime endDateTime = new DateTime(java.util.Date.from(endDateVal.atTime(entity.getEndTime()).atZone(ZoneId.systemDefault()).toInstant()));
            EventDateTime end = new EventDateTime().setDateTime(endDateTime).setTimeZone(ZoneId.systemDefault().getId());
            event.setEnd(end);

            // 1. Đồng bộ Quy luật lặp (RRULE)
            if (entity.getRecurrenceType() != null && !entity.getRecurrenceType().trim().isEmpty() && !"NONE".equalsIgnoreCase(entity.getRecurrenceType())) {
                StringBuilder rrule = new StringBuilder("RRULE:FREQ=").append(entity.getRecurrenceType().toUpperCase());
                if (entity.getRecurrenceEndDate() != null) {
                    String until = entity.getRecurrenceEndDate().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "T235959Z";
                    rrule.append(";UNTIL=").append(until);
                }
                event.setRecurrence(Collections.singletonList(rrule.toString()));
            }

            if (entity.getGoogleEventId() != null) {
                // PATCH (update)
                Event updatedEvent = client.events().patch("primary", entity.getGoogleEventId(), event).execute();
                entity.setSyncStatus(SyncStatus.SUCCESS);
                entity.setLastSyncedAt(LocalDateTime.now());
                log.info("Cập nhật thành công sự kiện lên Google Calendar: googleEventId={}", updatedEvent.getId());
            } else {
                // POST (create)
                Event createdEvent = client.events().insert("primary", event).execute();
                entity.setGoogleEventId(createdEvent.getId());
                entity.setSyncStatus(SyncStatus.SUCCESS);
                entity.setLastSyncedAt(LocalDateTime.now());
                log.info("Tạo mới thành công sự kiện lên Google Calendar: googleEventId={}", createdEvent.getId());
            }
        } catch (Exception e) {
            log.error("Failed to sync event to Google Calendar", e);
            entity.setSyncStatus(SyncStatus.FAILED);
        }
        calendarEventRepository.save(entity);
    }

    public void deleteEventFromGoogle(CalendarEventEntity entity) {
        if (entity == null || entity.getGoogleEventId() == null) return;

        UserEntity user = entity.getCreatedBy();
        if (user == null || user.getCurrentPlan() != PlanEnum.BUSINESS) {
            return;
        }

        try {
            Calendar client = getCalendarClient(user);
            if (client != null) {
                client.events().delete("primary", entity.getGoogleEventId()).execute();
                log.info("Xóa thành công sự kiện trên Google Calendar: googleEventId={}", entity.getGoogleEventId());
            }
        } catch (Exception e) {
            log.error("Failed to delete event from Google Calendar", e);
        }
    }

    public void syncOldEvents(UUID userId) {

//        Idea như này:
//        - Lấy hết lịch ở trong space personal của user
//        - Lọc ra 2 mảng: Có googleToken và không có
//                + Mảng có token thì dùng để check xem khi sync từ calendar thì cái lịch đã đc sync chưa
//                + Mảng ko có thì dùng để sync ngược lên calendar
//        Triển

        UserEntity user = userRepository.findById(userId).orElse(null);

        if (user == null || user.getCurrentPlan() != PlanEnum.BUSINESS) {
            return;
        }

        // Lọc ra 2 array
        List<CalendarEventEntity> events = calendarEventRepository.findByCreatedByIdAndSpaceId(user.getId(), user.getPersonalCalendarId());

        // Chỗ này dùng Set để lúc search ở dưới nó nhanh hơn O^n
        Set<String> eventsWithGoogleId = new HashSet<>();
        List<CalendarEventEntity> eventsWithoutGoogleId = new ArrayList<>();

        for (CalendarEventEntity event : events) {
            if (event.getGoogleEventId() != null) {
                eventsWithGoogleId.add(event.getGoogleEventId());
            } else {
                eventsWithoutGoogleId.add(event);
            }
        }

        // lấy từ calendar qua trước đã
        String oneMonthAgo = ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"))
                .minusMonths(1)
                .toInstant()
                .toString();
        GoogleCalendarEventsResponse calendarRes = this.getEventsFromCalendar(user.getGoogleCalendarAccessToken(), oneMonthAgo, null);

        SpaceEntity personalCalendar = spaceService.getSpaceById(user.getPersonalCalendarId());
        if (!calendarRes.items().isEmpty()) {
            for (GoogleCalendarEvent googleEvent : calendarRes.items()) {
                // Lịch nào có bên calendar mà chưa có bên này thì lưu
                if (!eventsWithGoogleId.contains(googleEvent.id())) {
                    CalendarEventEntity newEvent = googleCalendarUtils.mapGoogleEventToEntity(
                            googleEvent,
                            user,
                            personalCalendar
                    );

                    calendarEventRepository.save(newEvent);
                }
            }
        }

        // Tạo qua bên calendar
        for (CalendarEventEntity event : eventsWithoutGoogleId) {
            syncEventToGoogle(event.getId());
        }
    }

    public GoogleCalendarEventsResponse getEventsFromCalendar(
            String accessToken,
            String timeMin,
            String timeMax
    ) {
        return restClient.get()
                .uri(uriBuilder -> {
                    uriBuilder
                            .scheme("https")
                            .host("www.googleapis.com")
                            .path("/calendar/v3/calendars/primary/events")
                            .queryParam("singleEvents", true)
                            .queryParam("orderBy", "startTime");

                    if (timeMin != null) {
                        uriBuilder.queryParam("timeMin", timeMin);
                    }

                    if (timeMax != null) {
                        uriBuilder.queryParam("timeMax", timeMax);
                    }

                    return uriBuilder.build();
                })
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .body(GoogleCalendarEventsResponse.class);
    }
}
