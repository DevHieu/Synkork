package com.synkork.backend.modules.collaboration.calendar;

import com.synkork.backend.modules.collaboration.calendar.enums.AttachmentTypeEnum;
import com.synkork.backend.modules.user.UserEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "event_attachments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventAttachmentEntity {

    @Id
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false, columnDefinition = "BINARY(16)")
    private CalendarEventEntity event;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by", nullable = false, columnDefinition = "BINARY(16)")
    private UserEntity uploadedBy;

    @Column(nullable = false, length = 500)
    private String fileUrl;

    @Column(length = 255)
    private String fileName;

    private Integer fileSizeKb;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AttachmentTypeEnum type;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime uploadedAt;
}