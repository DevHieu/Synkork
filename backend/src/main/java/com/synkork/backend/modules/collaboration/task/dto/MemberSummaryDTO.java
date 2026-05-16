package com.synkork.backend.modules.collaboration.task.dto;

import java.util.UUID;

import com.synkork.backend.modules.roomMember.RoomMemberEntity;

import lombok.Data;

@Data 
    public class MemberSummaryDTO {
        private UUID id;   
        private String name;
        private String avatarUrl;
        
        public MemberSummaryDTO(RoomMemberEntity e) {
            this.id = e.getId();
            this.name = e.getUser().getDisplayName();
            this.avatarUrl = e.getUser().getAvatarUrl();
        }
    }
