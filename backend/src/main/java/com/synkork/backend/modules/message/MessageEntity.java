package com.synkork.backend.modules.message;

import com.synkork.backend.common.base.BaseEntity;

import com.synkork.backend.modules.roomMember.RoomMemberEntity;
import com.synkork.backend.modules.space.SpaceEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "messages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MessageEntity extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "space_id", nullable = false)
    private SpaceEntity space;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private RoomMemberEntity sender;

    @Column(columnDefinition = "TEXT")
    private String content;

    private boolean deleted = false;
    private boolean pinned = false;

    @Enumerated(EnumType.STRING)
    private MessageTypeEnum type =  MessageTypeEnum.TEXT;

    @Column(name = "attachment_url")
    private String attachmentUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reply_to_id")
    private MessageEntity replyTo;
}
