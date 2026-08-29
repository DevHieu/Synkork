package com.synkork.backend.filter;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor =
                MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            String email = accessor.getFirstNativeHeader("X-User-Email");
            String userId = accessor.getFirstNativeHeader("X-User-Id");

            if (email != null && !email.isBlank()) {
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(email, null, Collections.emptyList());

                accessor.setUser(authToken);
                if (accessor.getSessionAttributes() != null) {
                    accessor.getSessionAttributes().put("userEmail", email);
                    if (userId != null && !userId.isBlank()) {
                        accessor.getSessionAttributes().put("userId", userId);
                    }
                }
            }
        }

        return message;
    }
}