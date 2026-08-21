package com.synkork.backend.modules.admin.changePassword.dto;

import com.synkork.backend.modules.admin.changePassword.PasswordResetRequestEntity;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class PasswordResetRequestResponse {

    private UUID id;
    private String username;
    private String displayName;
    private String email;
    private String role;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static PasswordResetRequestResponse from(PasswordResetRequestEntity request) {
        return PasswordResetRequestResponse.builder()
                .id(request.getId())
                .username(request.getUser().getUsername())
                .displayName(request.getUser().getDisplayName())
                .email(request.getUser().getEmail())
                .role(request.getUser().getRole() != null ? request.getUser().getRole().name().toLowerCase() : null)
                .status(request.getStatus() != null ? request.getStatus().name() : null)
                .createdAt(request.getCreatedAt())
                .updatedAt(request.getUpdatedAt())
                .build();
    }
}
