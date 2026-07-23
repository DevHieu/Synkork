package com.synkork.backend.modules.admin.changePassword.email;

import com.synkork.backend.common.utils.EmailService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class PasswordResetRequestEmailService {

    private final EmailService emailService;

    public PasswordResetRequestEmailService(EmailService emailService) {
        this.emailService = emailService;
    }

    @Async
    public void sendOtpEmail(String to, String otp) {
        if (isBlank(to)) {
            return;
        }

        String subject = "[Synkork] Ma OTP xac thuc cua ban";
        String body = """
                <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;">
                    <h2 style="color: #023c3d;">Xac thuc yeu cau doi mat khau</h2>
                    <p>Ma OTP cua ban la:</p>
                    <div style="font-size: 32px; font-weight: bold; letter-spacing: 8px;
                                color: #023c3d; margin: 24px 0; text-align: center;">
                        %s
                    </div>
                    <p style="color: #888; font-size: 13px;">Ma co hieu luc trong 5 phut.</p>
                    <p style="color: #888; font-size: 13px;">Neu ban khong yeu cau ma nay, hay bo qua email.</p>
                </div>
                """.formatted(escapeHtml(otp));

        emailService.send(to, subject, body);
    }

    @Async
    public void sendApprovedEmail(String to) {
        if (isBlank(to)) {
            return;
        }

        String subject = "[Synkork] Mật khẩu của bạn đã được đặt lại";
        String body = """
                <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;">
                    <h2 style="color: #023c3d;">Mật khẩu đã được cập nhật</h2>
                    <p>Yêu cầu đặt lại mật khẩu của bạn đã được admin duyệt.</p>
                    <p>Bạn có thể đăng nhập với mật khẩu mới ngay bây giờ.</p>
                    <p style="color: #888; font-size: 13px;">Nếu bạn không thực hiện yêu cầu này, hãy liên hệ admin ngay.</p>
                </div>
                """;

        emailService.send(to, subject, body);
    }

    @Async
    public void sendRejectedEmail(String to) {
        if (isBlank(to)) {
            return;
        }

        String subject = "[Synkork] Yêu cầu đổi mật khẩu đã bị từ chối";
        String body = """
                <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;">
                    <h2 style="color: #dc2626;">Yêu cầu đổi mật khẩu bị từ chối</h2>
                    <p>Yêu cầu đặt lại mật khẩu của bạn đã được admin xem xét và bị từ chối.</p>
                    <p>Mật khẩu hiện tại của tài khoản vẫn được giữ nguyên.</p>
                    <p style="color: #888; font-size: 13px;">Nếu bạn cho rằng đây là nhầm lẫn, vui lòng liên hệ admin để được hỗ trợ.</p>
                </div>
                """;

        emailService.send(to, subject, body);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
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
