package com.synkork.backend.filter;

import com.synkork.backend.security.JwtService;
import com.synkork.backend.security.MyUserDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;

@Component
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    @Autowired
    private JwtService jwtService;

    @Autowired
    private MyUserDetailService userDetailService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor =
                MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor == null || !StompCommand.CONNECT.equals(accessor.getCommand())) {
            return message;
        }

        String authHeader = accessor.getFirstNativeHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new MessagingException("Unauthorized");
        }

        String token = authHeader.substring(7);
        try {
            String username = jwtService.extractUserName(token);
            String tokenType = jwtService.extractClaim(token,
                    claims -> claims.get("type", String.class));

            if (username == null) {
                throw new MessagingException("Unauthorized: missing username in token");
            }

            UserDetails userDetails = userDetailService.loadUserByUsername(username);

            if (!"ACCESS".equals(tokenType) || !jwtService.validateToken(token, userDetails)) {
                throw new MessagingException("Unauthorized: invalid token type or expired");
            }

            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());

            accessor.setUser(authToken);

            String userId = jwtService.extractClaim(token, claims -> claims.get("userId", String.class));
            accessor.getSessionAttributes().put("userId", userId);
            System.out.println("userId = " + userId);

        } catch (MessagingException e) {
            throw e;
        } catch (Exception e) {
            throw new MessagingException("JWT validation failed: " + e.getMessage());
        }

        return message;
    }
}