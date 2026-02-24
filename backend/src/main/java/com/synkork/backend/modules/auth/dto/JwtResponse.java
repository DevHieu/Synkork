package com.synkork.backend.modules.auth.dto;

import lombok.Builder;

@Builder
public record JwtResponse(
        String accessToken,
        String refreshToken
) {}
