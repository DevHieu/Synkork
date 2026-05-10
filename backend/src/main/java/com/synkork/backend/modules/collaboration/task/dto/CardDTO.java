package com.synkork.backend.modules.collaboration.task.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import com.synkork.backend.modules.collaboration.task.card.CardEntity;
import com.synkork.backend.modules.roomMember.RoomMemberEntity;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
public class CardDTO {
    private UUID id;
    private String title;
    private String description;
    private int position;

    private UUID spaceId;
    private UUID columnId;

    // private UUID assigneeId;
    
    // private Set<UUID> assigneeIds;

    private List<MemberSummaryDTO> assignees;

    private MemberSummaryDTO createdBy;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data 
    public class MemberSummaryDTO {
        private UUID id;      // memberId
        private String name;
        private String avatarUrl;
        
        public MemberSummaryDTO(RoomMemberEntity e) {
            this.id = e.getId();
            this.name = e.getUser().getDisplayName(); // hoặc field tương ứng
            this.avatarUrl = e.getUser().getAvatarUrl();
        }
    }

    public CardDTO(CardEntity e) {
        this.id = e.getId();
        this.title = e.getTitle();
        this.description = e.getDescription();
        this.position = e.getPosition();
        this.spaceId = e.getColumn().getSpace().getId();
        this.columnId = e.getColumn().getId();
        // if (e.getAssignee() != null) {
        //     this.assigneeId = e.getAssignee().getId();
        // }

        this.assignees = e.getAssignees().stream()
                .map(MemberSummaryDTO::new)
                .collect(Collectors.toList());

        if (e.getCreatedBy() != null) {
            this.createdBy = new MemberSummaryDTO(e.getCreatedBy());
        }

        this.createdAt = e.getCreatedAt();
    }
}
