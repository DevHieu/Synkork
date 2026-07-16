package com.synkork.backend.modules.admin.users.email;

import com.synkork.backend.common.utils.EmailService;
import com.synkork.backend.modules.user.UserEntity;
import com.synkork.backend.modules.user.enums.PlanEnum;
import com.synkork.backend.modules.user.enums.RoleEnum;
import com.synkork.backend.modules.user.enums.UserStatusEnum;
import org.springframework.stereotype.Service;

@Service
public class AdminUserEmailService {

    private final EmailService emailService;

    public AdminUserEmailService(EmailService emailService) {
        this.emailService = emailService;
    }

    public void sendWelcomeEmail(String email, String username, String tempPassword) {
        if (isBlank(email)) {
            return;
        }

        String body = plainTextEmailBody(String.format(
                "Xin chao %s,\n\nMat khau tam thoi: %s\n\n"
                        + "Vui long doi mat khau sau khi dang nhap.",
                username,
                tempPassword
        ));
        emailService.send(email, "[Synkork] Tai khoan cua ban da duoc tao", body);
    }

    public void sendUserUpdatedEmail(
            UserEntity user,
            String oldDisplayName,
            String oldEmail,
            PlanEnum oldPlan,
            UserStatusEnum oldStatus,
            RoleEnum oldRole
    ) {
        if (hasNoEmail(user)) {
            return;
        }

        String body = plainTextEmailBody(String.format(
                "Xin chao %s,\n\nTai khoan Synkork cua ban da duoc cap nhat.\n\n"
                        + "Thong tin truoc do:\n"
                        + "- Ten hien thi: %s\n"
                        + "- Email: %s\n"
                        + "- Goi: %s\n"
                        + "- Trang thai: %s\n"
                        + "- Vai tro: %s\n\n"
                        + "Thong tin hien tai:\n"
                        + "- Ten hien thi: %s\n"
                        + "- Email: %s\n"
                        + "- Goi: %s\n"
                        + "- Trang thai: %s\n"
                        + "- Vai tro: %s\n\n"
                        + "Neu ban khong yeu cau thay doi nay, vui long lien he quan tri vien.",
                user.getUsername(),
                valueOrDash(oldDisplayName),
                valueOrDash(oldEmail),
                valueOrDash(oldPlan),
                valueOrDash(oldStatus),
                valueOrDash(oldRole),
                valueOrDash(user.getDisplayName()),
                valueOrDash(user.getEmail()),
                valueOrDash(user.getCurrentPlan()),
                valueOrDash(user.getStatus()),
                valueOrDash(user.getRole())
        ));
        emailService.send(user.getEmail(), "[Synkork] Tai khoan cua ban da duoc cap nhat", body);
    }

    public void sendUserDeletedEmail(UserEntity user, String reason) {
        if (hasNoEmail(user)) {
            return;
        }

        String body = plainTextEmailBody(String.format(
                "Xin chao %s,\n\nTai khoan Synkork cua ban da duoc chuyen sang trang thai INACTIVE.\n\n"
                        + "Ly do: %s\n\n"
                        + "Ban da duoc xoa khoi tat ca room dang tham gia. "
                        + "Neu can ho tro them, vui long lien he quan tri vien.",
                user.getUsername(),
                reason
        ));
        emailService.send(user.getEmail(), "[Synkork] Tai khoan cua ban da bi khoa", body);
    }

    public void sendUserLockedEmail(UserEntity user) {
        if (hasNoEmail(user)) {
            return;
        }

        String targetName = escapeHtml(getDisplayName(user));
        String subject = "[Synkork] Thong bao khoa tai khoan";
        String body = """
                <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;
                            padding: 24px; border: 1px solid #e5e7eb; border-radius: 12px;">
                    <h2 style="color: #dc2626;">Thong bao khoa tai khoan</h2>

                    <p style="color: #374151;">
                        Quan tri vien da khoa tai khoan <strong>%s</strong> do vi pham quy dinh cua Synkork.
                    </p>

                    <div style="margin: 24px 0; padding: 16px; background: #fef2f2;
                                border-left: 4px solid #ef4444; border-radius: 8px;">
                        <p style="margin: 0; color: #991b1b;">
                            Trang thai hien tai: <strong>Da bi khoa</strong>
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
                """.formatted(targetName);

        emailService.send(user.getEmail(), subject, body);
    }

    public void sendUserWarningEmail(UserEntity user) {
        if (hasNoEmail(user)) {
            return;
        }

        String targetName = escapeHtml(getDisplayName(user));
        String subject = "[Synkork] Canh bao vi pham";
        String body = """
                <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;
                            padding: 24px; border: 1px solid #e5e7eb; border-radius: 12px;">
                    <h2 style="color: #b45309;">Canh bao vi pham</h2>

                    <p style="color: #374151;">
                        Xin chao, quan tri vien da gui canh bao den tai khoan <strong>%s</strong>.
                    </p>

                    <div style="margin: 24px 0; padding: 16px; background: #fffbeb;
                                border-left: 4px solid #f59e0b; border-radius: 8px;">
                        <p style="margin: 0; color: #92400e;">
                            Tong so lan canh bao hien tai: <strong>%d</strong>
                        </p>
                    </div>

                    <p style="color: #374151;">
                        Vui long kiem tra va dieu chinh hanh vi su dung de tranh bi han che hoac khoa tai khoan trong tuong lai.
                    </p>

                    <hr style="border: none; border-top: 1px solid #e5e7eb; margin: 24px 0 16px;"/>

                    <p style="margin: 0; font-size: 12px; color: #9ca3af; text-align: center;">
                        Day la email duoc gui tu dong tu Synkork. Vui long khong tra loi email nay.
                    </p>
                </div>
                """.formatted(targetName, user.getWarning());

        emailService.send(user.getEmail(), subject, body);
    }

    private boolean hasNoEmail(UserEntity user) {
        return user == null || isBlank(user.getEmail());
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private String getDisplayName(UserEntity user) {
        return user.getDisplayName() != null && !user.getDisplayName().isBlank()
                ? user.getDisplayName()
                : user.getUsername();
    }

    private String valueOrDash(Object value) {
        return value == null ? "-" : value.toString();
    }

    private String plainTextEmailBody(String text) {
        return "<div style=\"font-family: Arial, sans-serif; white-space: pre-line;\">"
                + escapeHtml(text)
                + "</div>";
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
