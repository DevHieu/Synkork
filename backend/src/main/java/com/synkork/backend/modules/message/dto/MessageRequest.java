package com.synkork.backend.modules.message.dto;

public record MessageRequest(String content, Integer version, String replyToId) {
}
