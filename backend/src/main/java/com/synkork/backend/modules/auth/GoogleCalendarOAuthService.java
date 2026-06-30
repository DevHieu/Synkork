package com.synkork.backend.modules.auth;

import com.synkork.backend.modules.auth.dto.GoogleCalendarTokenResponse;
import com.synkork.backend.modules.user.UserEntity;
import com.synkork.backend.modules.user.UserRepository;
import com.synkork.backend.modules.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class GoogleCalendarOAuthService {
    @Value("${google.calendar.client-id}")
    private String clientId;

    @Value("${google.calendar.client-secret}")
    private String clientSecret;

    @Value("${google.calendar.redirect-uri}")
    private String redirectUri;

    @Autowired
    private UserService userService;

    private final RestClient restClient = RestClient.create();

    public String buildAuthorizeUrl(String state) {
        return UriComponentsBuilder.fromHttpUrl("https://accounts.google.com/o/oauth2/v2/auth")
                .queryParam("client_id", clientId)
                .queryParam("redirect_uri", redirectUri)
                .queryParam("response_type", "code")
                .queryParam("scope", "https://www.googleapis.com/auth/calendar")
                .queryParam("access_type", "offline")
                .queryParam("prompt", "consent")
                .queryParam("state", state)
                .toUriString();
    }

    public void handleCallback(String code, UUID userId) {
        GoogleCalendarTokenResponse tokenResponse = exchangeCodeForToken(code);

        UserEntity user = userService.findById(userId);

        user.setGoogleCalendarAccessToken(tokenResponse.getAccess_token());
        user.setGoogleCalendarAccessTokenExpiresAt(LocalDateTime.now().plusSeconds(tokenResponse.getExpires_in()));

        // Quan trọng: chỉ ghi đè refreshToken nếu Google có trả về (không phải lúc nào cũng có)
        if (tokenResponse.getRefresh_token() != null) {
            user.setGoogleCalendarRefreshToken(tokenResponse.getRefresh_token());
        }

        userService.updateUser(user);
    }

    private GoogleCalendarTokenResponse exchangeCodeForToken(String code) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("client_id", clientId);
        form.add("client_secret", clientSecret);
        form.add("code", code);
        form.add("redirect_uri", redirectUri);
        form.add("grant_type", "authorization_code");

        return restClient.post()
                .uri("https://oauth2.googleapis.com/token")
                .body(form)
                .retrieve()
                .body(GoogleCalendarTokenResponse.class);
    }
}
