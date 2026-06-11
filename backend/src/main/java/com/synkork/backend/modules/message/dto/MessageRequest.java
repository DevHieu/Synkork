package com.synkork.backend.modules.message.dto;

public record MessageRequest(String content, String replyToId) {
}
