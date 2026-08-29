package com.synkork.backend.modules.auth;

import com.synkork.backend.common.utils.AuthUtils;
import com.synkork.backend.modules.auth.GoogleCalendarOAuthService;
import com.synkork.backend.modules.collaboration.calendar.service.GoogleCalendarService;
import com.synkork.backend.modules.user.UserEntity;
import com.synkork.backend.modules.user.UserRepository;
import com.synkork.backend.modules.user.enums.PlanEnum;
import com.synkork.backend.security.JwtService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/integrations")
public class IntegrationController {

    @Value("${frontend.client.url}")
    private String frontendUrl;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private GoogleCalendarOAuthService googleCalendarOAuthService;

    @Autowired
    private GoogleCalendarService googleCalendarService;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/google-calendar/authorize-url")
    public ResponseEntity<?> getAuthorizeUrl(@RequestParam(required = false) String redirectPath) {
        UUID userId = AuthUtils.getCurrentUserId();
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng với ID: " + userId));

        if (user.getCurrentPlan() != PlanEnum.BUSINESS) {
            return ResponseEntity.status(403).body(Map.of("message", "Tính năng Đồng bộ Google Calendar chỉ dành cho gói Business."));
        }

        String state = jwtService.generateShortLivedState(userId.toString(), redirectPath);
        return ResponseEntity.ok(Map.of("authorizeUrl", googleCalendarOAuthService.buildAuthorizeUrl(state)));
    }

    @GetMapping("/google-calendar/callback")
    public RedirectView handleCallback(
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) String error
    ) {
        if (error != null) {
            return new RedirectView(frontendUrl + "/me?sync=error");
        }
        
        try {
            var claims = jwtService.validateAndExtractState(state);
            String userId = claims.get("userId", String.class);
            String redirectPath = claims.get("redirectPath", String.class);
            
            if (redirectPath == null) redirectPath = "/me";
            
            googleCalendarOAuthService.handleCallback(code, UUID.fromString(userId));
            
            googleCalendarService.syncOldEvents(UUID.fromString(userId));
            
            return new RedirectView(frontendUrl + redirectPath + "?sync=success");
        } catch (Exception e) {
            e.printStackTrace();
            return new RedirectView(frontendUrl + "/me?sync=error");
        }
    }
}