package com.synkork.backend.modules.report;

import com.synkork.backend.common.base.BaseEntity;
import com.synkork.backend.modules.report.enums.ReportReasonEnums;
import com.synkork.backend.modules.report.enums.ReportSeverityEnums;
import com.synkork.backend.modules.report.enums.ReportStatusEnums;
import com.synkork.backend.modules.report.enums.ReportTypeEnums;
import com.synkork.backend.modules.room.RoomEntity;
import com.synkork.backend.modules.user.UserEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "reports")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReportEntity extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_user_id", nullable = true)
    private UserEntity targetUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_room_id", nullable = true)
    private RoomEntity targetRoom;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportReasonEnums reason;

    private String description;

    @Enumerated(EnumType.STRING)
    private ReportTypeEnums reportType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id", nullable = false, updatable = false)
    private UserEntity reporter;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ReportStatusEnums status = ReportStatusEnums.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportSeverityEnums severity;
}