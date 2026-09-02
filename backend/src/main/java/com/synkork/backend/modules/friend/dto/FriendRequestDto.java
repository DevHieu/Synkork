package com.synkork.backend.modules.friend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record FriendRequestDto(
        @NotNull(message = "ID không được bỏ trống")
        UUID id,

        @NotBlank(message = "Tên người gửi không được bỏ trống")
        String senderName,

        @NotBlank(message = "Tên người nhận không được bỏ trống")
        String receiverName,

        @NotBlank(message = "Username người gửi không được bỏ trống")
        String senderUsername,

        @NotBlank(message = "Username người nhận không được bỏ trống")
        String receiverUsername,

        @NotBlank(message = "Tên người gửi không được bỏ trống")
        String status
) {}