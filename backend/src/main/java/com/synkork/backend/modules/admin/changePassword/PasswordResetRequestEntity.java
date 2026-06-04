package com.synkork.backend.modules.admin.changePassword;

import com.synkork.backend.common.base.BaseEntity;
import com.synkork.backend.modules.admin.changePassword.enums.PasswordResetStatusEnum;
import com.synkork.backend.modules.user.UserEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "password_reset_requests")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PasswordResetRequestEntity extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(nullable = false)
    private String newPassword;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private PasswordResetStatusEnum status = PasswordResetStatusEnum.PENDING;
}