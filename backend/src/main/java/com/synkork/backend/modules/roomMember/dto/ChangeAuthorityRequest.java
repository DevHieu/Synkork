package com.synkork.backend.modules.roomMember.dto;

import jakarta.validation.constraints.NotNull;

public record ChangeAuthorityRequest(

        @NotNull(message = "Id member không được bỏ trống")
        String memberId,

        @NotNull(message = "Role mới không được bỏ trống")
        String newRole) {
}
