package com.synkork.backend.modules.friend.dto;

import java.util.UUID;

public record FriendDto(
        UUID id,
        String name,
        String username,
        String avatarUrl,
        boolean isOnline,
        UUID conversationId
) {}