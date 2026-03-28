package com.synkork.backend.modules.friend.dto;

import java.util.UUID;

public record FriendDto(
        UUID id,
        String name,
        String avatarUrl,
        String status
) {}