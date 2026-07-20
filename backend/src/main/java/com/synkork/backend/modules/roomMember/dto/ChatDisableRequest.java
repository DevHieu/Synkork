package com.synkork.backend.modules.roomMember.dto;

import com.synkork.backend.modules.roomMember.enums.ChatDisableTime;

public record ChatDisableRequest(ChatDisableTime time) {
}
