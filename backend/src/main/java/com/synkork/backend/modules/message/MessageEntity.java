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
    private boolean edited = false;

    @Enumerated(EnumType.STRING)
    private MessageTypeEnum type =  MessageTypeEnum.TEXT;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reply_to_id")
    private MessageEntity replyTo;

    @Column(name = "attachment_url")
    private String attachmentUrl;

    @Column(name = "attachment_public_id")
    private String attachmentPublicId;  // cần để xóa trên Cloudinary

    @Column(name = "attachment_resource_type")
    private String attachmentResourceType;  // "image", "video" hoặc "raw" để xóa đúng

    @Column(name = "attachment_name")
    private String attachmentName;  // tên file gốc để hiển thị cho user

    @Version
    private Integer version;
}
