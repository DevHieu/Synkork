package com.synkork.backend.modules.friend.dto;

import java.util.UUID;

public record FriendRequestDto(
        UUID id,
        String senderName,
        String receiverName,
        String senderUsername,
        String  receiverUsername,
        String status
) {}