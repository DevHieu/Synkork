package com.synkork.backend.modules.admin.report.email;

import com.synkork.backend.common.utils.EmailService;
import com.synkork.backend.modules.report.enums.ReportStatusEnums;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class AdminReportEmailService {

    private final EmailService emailService;

    public AdminReportEmailService(EmailService emailService) {
        this.emailService = emailService;
    }

    @Async
    public void sendReportResolvedEmail(String toEmail, String reporterName, String note, ReportStatusEnums status) {
        if (isBlank(toEmail)) {
            return;
        }

        String statusText;
        String statusColor;
        switch (status) {
            case RESOLVED:
                statusText = "Da xu ly vi pham";
                statusColor = "#22c55e";
                break;
            case DISMISSED:
                statusText = "Khong du can cu xu ly";
                statusColor = "#ef4444";
                break;
            default:
                statusText = status.name();
                statusColor = "#6b7280";
                break;
        }

        String subject = "[Synkork] Ket qua xu ly to cao";
        String noteBlock = !isBlank(note)
                ? """
                        <p style="margin: 8px 0 0 0; color: #374151;">
                            <strong>Ghi chu:</strong> %s
                        </p>
                        """.formatted(escapeHtml(note))
                : "";

        String body = """
                <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;
                            padding: 24px; border: 1px solid #e5e7eb; border-radius: 12px;">
                    <h2 style="color: #023c3d;">Ket qua xu ly to cao</h2>

                    <p style="color: #374151;">
                        Xin chao <strong>%s</strong>, to cao cua ban da duoc quan tri vien xem xet.
                    </p>

                    <div style="margin: 24px 0; padding: 16px; background: #f9fafb;
                                border-left: 4px solid %s; border-radius: 8px;">
                        <p style="margin: 0; color: #111827;">
                            Ket qua: <strong style="color: %s;">%s</strong>
                        </p>
                        %s
                    </div>

                    <p style="color: #374151;">
                        Cam on ban da giup xay dung cong dong Synkork an toan hon.
                    </p>

                    <hr style="border: none; border-top: 1px solid #e5e7eb; margin: 24px 0 16px;"/>

                    <p style="margin: 0; font-size: 12px; color: #9ca3af; text-align: center;">
                        Day la email duoc gui tu dong tu Synkork. Vui long khong tra loi email nay.
                    </p>
                </div>
                """.formatted(
                escapeHtml(displayNameOrFallback(reporterName)),
                statusColor,
                statusColor,
                escapeHtml(statusText),
                noteBlock
        );

        emailService.send(toEmail, subject, body);
    }

    private String displayNameOrFallback(String value) {
        return isBlank(value) ? "ban" : value;
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
