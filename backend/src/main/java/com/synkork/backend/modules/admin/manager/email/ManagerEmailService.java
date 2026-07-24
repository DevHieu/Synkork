package com.synkork.backend.modules.admin.manager.email;

import com.synkork.backend.common.utils.EmailService;
import com.synkork.backend.modules.user.UserEntity;
import com.synkork.backend.modules.user.enums.RoleEnum;
import com.synkork.backend.modules.user.enums.UserStatusEnum;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class ManagerEmailService {

    private final EmailService emailService;

    public ManagerEmailService(EmailService emailService) {
        this.emailService = emailService;
    }

    @Async
    public void sendManagerAccessEmail(UserEntity account, String temporaryPassword) {
        if (hasNoEmail(account)) {
            return;
        }

        String subject = "[Synkork] Tai khoan quan tri cua ban da duoc tao";
        String body = """
                <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;
                            padding: 24px; border: 1px solid #e5e7eb; border-radius: 12px;">
                    <h2 style="color: #023c3d;">Tai khoan quan tri da duoc tao</h2>

                    <p style="color: #374151;">
                        Xin chao <strong>%s</strong>,<br/>
                        Quan tri vien da tao tai khoan %s cho ban tren Synkork.
                    </p>

                    <div style="margin: 24px 0; padding: 16px; background: #f0fdf4;
                                border-left: 4px solid #22c55e; border-radius: 8px;">
                        <p style="margin: 0; color: #166534; line-height: 1.6;">
                            <strong>Email dang nhap:</strong> %s<br/>
                            <strong>Username:</strong> %s<br/>
                            <strong>Mat khau tam thoi:</strong> %s<br/>
                            <strong>Vai tro:</strong> %s
                        </p>
                    </div>

                    <p style="color: #374151;">
                        Vui long dang nhap va doi mat khau sau lan truy cap dau tien.
                    </p>

                    <hr style="border: none; border-top: 1px solid #e5e7eb; margin: 24px 0 16px;"/>

                    <p style="margin: 0; font-size: 12px; color: #9ca3af; text-align: center;">
                        Day la email duoc gui tu dong tu Synkork. Vui long khong tra loi email nay.
                    </p>
                </div>
                """.formatted(
                escapeHtml(getDisplayName(account)),
                escapeHtml(valueOrDash(account.getRole())),
                escapeHtml(account.getEmail()),
                escapeHtml(valueOrDash(account.getUsername())),
                escapeHtml(valueOrDash(temporaryPassword)),
                escapeHtml(valueOrDash(account.getRole()))
        );

        emailService.send(account.getEmail(), subject, body);
    }

    @Async
    public void sendManagerLockedEmail(UserEntity account, String reason) {
        if (hasNoEmail(account)) {
            return;
        }

        String subject = "[Synkork] Tai khoan quan tri cua ban da bi khoa";
        String body = """
                <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;
                            padding: 24px; border: 1px solid #e5e7eb; border-radius: 12px;">
                    <h2 style="color: #dc2626;">Thong bao khoa tai khoan quan tri</h2>

                    <p style="color: #374151;">
                        Xin chao <strong>%s</strong>,<br/>
                        Tai khoan quan tri Synkork cua ban da bi khoa boi quan tri vien.
                    </p>

                    <div style="margin: 24px 0; padding: 16px; background: #fef2f2;
                                border-left: 4px solid #ef4444; border-radius: 8px;">
                        <p style="margin: 0; color: #991b1b; line-height: 1.6;">
                            <strong>Trang thai hien tai:</strong> %s<br/>
                            <strong>Ly do:</strong> %s
                        </p>
                    </div>

                    <p style="color: #374151;">
                        Neu ban cho rang day la su nham lan, vui long lien he doi ngu ho tro Synkork de duoc xem xet.
                    </p>

                    <hr style="border: none; border-top: 1px solid #e5e7eb; margin: 24px 0 16px;"/>

                    <p style="margin: 0; font-size: 12px; color: #9ca3af; text-align: center;">
                        Day la email duoc gui tu dong tu Synkork. Vui long khong tra loi email nay.
                    </p>
                </div>
                """.formatted(
                escapeHtml(getDisplayName(account)),
                escapeHtml(valueOrDash(account.getStatus())),
                escapeHtml(valueOrDash(reason))
        );

        emailService.send(account.getEmail(), subject, body);
    }

    @Async
    public void sendManagerUpdatedEmail(
            UserEntity account,
            String oldDisplayName,
            String oldEmail,
            UserStatusEnum oldStatus,
            RoleEnum oldRole
    ) {
        if (hasNoEmail(account)) {
            return;
        }

        String subject = "[Synkork] Tai khoan quan tri cua ban da duoc cap nhat";
        String body = """
                <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;
                            padding: 24px; border: 1px solid #e5e7eb; border-radius: 12px;">
                    <h2 style="color: #023c3d;">Tai khoan quan tri da duoc cap nhat</h2>

                    <p style="color: #374151;">
                        Xin chao <strong>%s</strong>, tai khoan quan tri Synkork cua ban da duoc cap nhat.
                    </p>

                    <div style="margin: 24px 0; padding: 16px; background: #f9fafb;
                                border-left: 4px solid #023c3d; border-radius: 8px;">
                        <p style="margin: 0 0 8px 0; color: #111827; font-weight: 600;">Thong tin truoc do</p>
                        <p style="margin: 0; color: #374151; line-height: 1.6;">
                            <strong>Ten hien thi:</strong> %s<br/>
                            <strong>Email:</strong> %s<br/>
                            <strong>Trang thai:</strong> %s<br/>
                            <strong>Vai tro:</strong> %s
                        </p>
                    </div>

                    <div style="margin: 24px 0; padding: 16px; background: #f0fdf4;
                                border-left: 4px solid #22c55e; border-radius: 8px;">
                        <p style="margin: 0 0 8px 0; color: #166534; font-weight: 600;">Thong tin hien tai</p>
                        <p style="margin: 0; color: #166534; line-height: 1.6;">
                            <strong>Ten hien thi:</strong> %s<br/>
                            <strong>Email:</strong> %s<br/>
                            <strong>Trang thai:</strong> %s<br/>
                            <strong>Vai tro:</strong> %s
                        </p>
                    </div>

                    <p style="color: #374151;">
                        Neu ban khong yeu cau thay doi nay, vui long lien he quan tri vien.
                    </p>

                    <hr style="border: none; border-top: 1px solid #e5e7eb; margin: 24px 0 16px;"/>

                    <p style="margin: 0; font-size: 12px; color: #9ca3af; text-align: center;">
                        Day la email duoc gui tu dong tu Synkork. Vui long khong tra loi email nay.
                    </p>
                </div>
                """.formatted(
                escapeHtml(getDisplayName(account)),
                escapeHtml(valueOrDash(oldDisplayName)),
                escapeHtml(valueOrDash(oldEmail)),
                escapeHtml(valueOrDash(oldStatus)),
                escapeHtml(valueOrDash(oldRole)),
                escapeHtml(valueOrDash(account.getDisplayName())),
                escapeHtml(valueOrDash(account.getEmail())),
                escapeHtml(valueOrDash(account.getStatus())),
                escapeHtml(valueOrDash(account.getRole()))
        );

        emailService.send(account.getEmail(), subject, body);
    }

    private boolean hasNoEmail(UserEntity account) {
        return account == null || isBlank(account.getEmail());
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private String getDisplayName(UserEntity account) {
        return !isBlank(account.getDisplayName()) ? account.getDisplayName() : account.getUsername();
    }

    private String valueOrDash(Object value) {
        return value == null ? "-" : value.toString();
    }

    private String escapeHtml(String text) {
        if (text == null) {
            return "";
        }
        return text
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
