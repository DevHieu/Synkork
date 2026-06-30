package com.synkork.backend.modules.auth;

import com.synkork.backend.common.utils.AuthUtils;
import com.synkork.backend.modules.auth.GoogleCalendarOAuthService;
import com.synkork.backend.security.JwtService;
import jakarta.servlet.http.HttpServletResponse;
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

    @GetMapping("/google-calendar/authorize-url")
    public ResponseEntity<Map<String, String>> getAuthorizeUrl() {
        UUID userId = AuthUtils.getCurrentUserId();
        String state = jwtService.generateShortLivedState(userId.toString());
        return ResponseEntity.ok(Map.of("authorizeUrl", googleCalendarOAuthService.buildAuthorizeUrl(state)));
    }

    @GetMapping("/google-calendar/callback")
    public void handleCallback(
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) String error,
            HttpServletResponse response
    ) throws IOException {
        String resultStatus;
        try {
            if (error != null) {
                resultStatus = "error";
            } else {
                String userId = jwtService.validateAndExtractStateUserId(state);
                googleCalendarOAuthService.handleCallback(code, UUID.fromString(userId));
                resultStatus = "success";
            }
        } catch (Exception e) {
            resultStatus = "error";
        }

        response.setContentType("text/html");
        response.getWriter().write("""
            <html><body>
            <script>
                window.opener.postMessage({ type: "GOOGLE_CALENDAR_LINK", status: "%s" }, "%s");
                window.close();
            </script>
            </body></html>
        """.formatted(resultStatus, frontendUrl));
    }
}