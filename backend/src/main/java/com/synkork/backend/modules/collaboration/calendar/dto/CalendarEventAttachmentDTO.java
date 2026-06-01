package com.synkork.backend.modules.collaboration.calendar.dto;

import com.synkork.backend.modules.collaboration.calendar.entity.EventAttachmentEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CalendarEventAttachmentDTO {
    private String name;
    private Integer size;
    private String fileUrl;
    private String publicId;
    private String resourceType;
    private String type;

    public CalendarEventAttachmentDTO(EventAttachmentEntity entity) {
        this.name = entity.getFileName();
        this.size = entity.getFileSizeKb();
        this.fileUrl = entity.getFileUrl();
        this.publicId = entity.getPublicId();
        this.resourceType = entity.getResourceType();
        this.type = entity.getType() != null ? entity.getType().name() : null;
    }
}
