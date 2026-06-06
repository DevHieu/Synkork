package com.synkork.backend.modules.auth.dto;

public record ResetPasswordRequest(String email, String newPassword) {
}
