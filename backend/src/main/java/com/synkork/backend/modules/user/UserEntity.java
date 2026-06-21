package com.synkork.backend.modules.user;

import com.synkork.backend.common.base.BaseEntity;
import com.synkork.backend.modules.user.enums.PlanEnum;
import com.synkork.backend.modules.user.enums.ProviderEnum;
import com.synkork.backend.modules.user.enums.UserStatusEnum;

import com.synkork.backend.modules.user.enums.RoleEnum;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.*;
import org.checkerframework.common.aliasing.qual.Unique;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserEntity extends BaseEntity {

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(name = "display_name", length = 100)
    private String displayName;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    private String password;
    private String avatarUrl;
    private String avatarId;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ProviderEnum provider = ProviderEnum.LOCAL;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private RoleEnum role = RoleEnum.USER;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private UserStatusEnum status = UserStatusEnum.ACTIVE;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private PlanEnum currentPlan = PlanEnum.FREE;

    private LocalDateTime planExpiresAt;

    @Unique
    private UUID personalNoteId;

    @Unique
    private UUID personalCalendarId;

    @Column(length = 2048)
    private String googleCalendarRefreshToken;

    @Column(length = 2048)
    private String googleCalendarAccessToken;

    private LocalDateTime googleCalendarAccessTokenExpiresAt;
}
