package com.synkork.backend.modules.voice.dtos;

public record VoiceSummaryResponse(
        String message,
        String fileUrl,
        String publicId,
        String transcript,
        String summaryJson
) {}
