package com.synkork.backend.modules.collaboration.calendar.service;

import com.google.api.client.auth.oauth2.AuthorizationCodeRequestUrl;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.synkork.backend.common.utils.AuthUtils;
import com.synkork.backend.modules.collaboration.calendar.entity.CalendarEventEntity;
import com.synkork.backend.modules.collaboration.calendar.enums.SyncStatus;
import com.synkork.backend.modules.collaboration.calendar.repository.CalendarEventRepository;
import com.synkork.backend.modules.user.UserEntity;
import com.synkork.backend.modules.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class GoogleCalendarService {

    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final List<String> SCOPES = Collections.singletonList(CalendarScopes.CALENDAR_EVENTS);

    @Value("${google.client.id:${GOOGLE_CLIENT_ID:}}")
    private String clientId;

    @Value("${google.client.secret:${GOOGLE_CLIENT_SECRET:}}")
    private String clientSecret;

    @Value("${server.url:${SERVER_URL:http://localhost:8080}}")
    private String serverUrl;

    private final UserRepository userRepository;
    private final CalendarEventRepository calendarEventRepository;

    private String getRedirectUri() {
        return serverUrl + "/api/integrations/google-calendar/callback";
    }

    private Calendar getCalendarClient(UserEntity user) throws Exception {
        if (user.getGoogleCalendarAccessToken() == null) {
            return null; // Not connected
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
            if (user.getGoogleCalendarRefreshToken() == null) return;

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

    // --- Sync Methods ---

    public void syncEventToGoogleAsync(UUID eventId) {
        CompletableFuture.runAsync(() -> syncEventToGoogle(eventId));
    }

    public void syncEventToGoogle(UUID eventId) {
        CalendarEventEntity entity = calendarEventRepository.findById(eventId).orElse(null);
        if (entity == null) return;

        UserEntity user = entity.getCreatedBy();
        try {
            Calendar client = getCalendarClient(user);
            if (client == null) return;

            Event event = new Event()
                    .setSummary(entity.getTitle())
                    .setDescription(entity.getDescription());

            DateTime startDateTime = new DateTime(java.util.Date.from(entity.getEventDate().atTime(entity.getStartTime()).atZone(ZoneId.systemDefault()).toInstant()));
            EventDateTime start = new EventDateTime().setDateTime(startDateTime).setTimeZone(ZoneId.systemDefault().getId());
            event.setStart(start);

            DateTime endDateTime = new DateTime(java.util.Date.from(entity.getEventDate().atTime(entity.getEndTime()).atZone(ZoneId.systemDefault()).toInstant()));
            EventDateTime end = new EventDateTime().setDateTime(endDateTime).setTimeZone(ZoneId.systemDefault().getId());
            event.setEnd(end);

            if (entity.getGoogleEventId() != null) {
                // PATCH (update)
                Event updatedEvent = client.events().patch("primary", entity.getGoogleEventId(), event).execute();
                entity.setSyncStatus(SyncStatus.SUCCESS);
                entity.setLastSyncedAt(LocalDateTime.now());
            } else {
                // POST (create)
                Event createdEvent = client.events().insert("primary", event).execute();
                entity.setGoogleEventId(createdEvent.getId());
                entity.setSyncStatus(SyncStatus.SUCCESS);
                entity.setLastSyncedAt(LocalDateTime.now());
            }
        } catch (Exception e) {
            log.error("Failed to sync event", e);
            entity.setSyncStatus(SyncStatus.FAILED);
        }
        calendarEventRepository.save(entity);
    }

    public void deleteEventFromGoogleAsync(UUID eventId) {
        CompletableFuture.runAsync(() -> {
            CalendarEventEntity entity = calendarEventRepository.findById(eventId).orElse(null);
            if (entity == null || entity.getGoogleEventId() == null) return;

            UserEntity user = entity.getCreatedBy();
            try {
                Calendar client = getCalendarClient(user);
                if (client != null) {
                    client.events().delete("primary", entity.getGoogleEventId()).execute();
                }
            } catch (Exception e) {
                log.error("Failed to delete event", e);
            }
        });
    }

    public void syncOldEvents(UUID userId) {
        List<CalendarEventEntity> oldEvents = calendarEventRepository.findByCreatedByIdAndGoogleEventIdIsNull(userId);
        for (CalendarEventEntity event : oldEvents) {
            syncEventToGoogle(event.getId());
        }
    }
}
