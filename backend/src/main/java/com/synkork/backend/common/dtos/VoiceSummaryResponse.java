package com.synkork.backend.common.dtos;

public record VoiceSummaryResponse(
        String message,
        String fileUrl,
        String publicId,
        String transcript,
        String summaryJson
) {}
