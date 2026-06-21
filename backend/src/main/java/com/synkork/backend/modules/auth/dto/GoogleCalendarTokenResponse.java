package com.synkork.backend.modules.auth.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GoogleCalendarTokenResponse {
    private String access_token;
    private String refresh_token; // chỉ có ở lần consent đầu, hoặc khi prompt=consent
    private Long expires_in;
    private String scope;
    private String token_type;
}