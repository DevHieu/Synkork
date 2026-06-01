package com.synkork.backend.modules.collaboration.calendar.entity;

import com.synkork.backend.common.base.BaseEntity;
import com.synkork.backend.modules.collaboration.calendar.enums.AttachmentTypeEnum;
import com.synkork.backend.modules.user.UserEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "event_attachments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EventAttachmentEntity extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false, columnDefinition = "BINARY(16)")
    private CalendarEventEntity event;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by", nullable = false, columnDefinition = "BINARY(16)")
    private UserEntity uploadedBy;

    @Column(nullable = false, length = 500)
    private String fileUrl;

    @Column(length = 500)
    private String publicId;

    @Column(length = 255)
    private String fileName;

    private Integer fileSizeKb;

    @Column(length = 50)
    private String resourceType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AttachmentTypeEnum type;
}
