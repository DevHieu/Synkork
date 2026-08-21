package com.synkork.backend.modules.payment.utils;

import com.synkork.backend.common.utils.EmailService;
import com.synkork.backend.modules.user.enums.PlanEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class ExpiredSubscriptionEmail {

    @Value("${frontend.client.url}")
    private String frontendUrl;

    @Autowired
    private EmailService emailService;

    @Async
    public void sendRemindUserRenewSubscription(String toEmail, PlanEnum plan, long daysRemaining,
                                                List<String> pendingRooms,
                                                Map<String, List<String>> pendingSpaces) {
        String subject = "[Synkork] Gói " + plan + " sắp hết hạn";
        String urgencyColor = daysRemaining <= 1 ? "#ef4444" : "#f97316";
        String urgencyText = daysRemaining == 0 ? "hôm nay"
                : daysRemaining == 1 ? "ngày mai"
                : "trong " + daysRemaining + " ngày";

        // Build phần danh sách xóa
        StringBuilder deletionBlock = new StringBuilder();
        if (!pendingRooms.isEmpty() || !pendingSpaces.isEmpty()) {
            deletionBlock.append("""
                        <div style="margin: 16px 0; padding: 16px; background: #fef2f2;
                                    border-left: 4px solid #ef4444; border-radius: 8px;">
                            <p style="margin: 0 0 8px 0; font-weight: bold; color: #991b1b;">
                                ⚠️ Các phòng và kênh sẽ bị xóa nếu không gia hạn:
                            </p>
                    """);

            if (!pendingRooms.isEmpty()) {
                deletionBlock.append(
                        "<p style='margin: 4px 0; color: #7f1d1d;'><strong>Phòng bị xóa:</strong></p><ul style='margin: 4px 0; padding-left: 20px; color: #7f1d1d;'>");
                pendingRooms.forEach(r -> deletionBlock.append("<li>").append(r).append("</li>"));
                deletionBlock.append("</ul>");
            }

            if (!pendingSpaces.isEmpty()) {
                deletionBlock
                        .append("<p style='margin: 8px 0 4px 0; color: #7f1d1d;'><strong>Kênh bị xóa:</strong></p>");
                pendingSpaces.forEach((roomName, spaces) -> {
                    deletionBlock.append("<p style='margin: 4px 0; color: #7f1d1d;'>📁 ").append(roomName)
                            .append("</p>");
                    deletionBlock.append("<ul style='margin: 2px 0; padding-left: 20px; color: #7f1d1d;'>");
                    spaces.forEach(s -> deletionBlock.append("<li>").append(s).append("</li>"));
                    deletionBlock.append("</ul>");
                });
            }

            deletionBlock.append("</div>");
        }

        String body = """
                    <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;
                                padding: 24px; border: 1px solid #e5e7eb; border-radius: 12px;">
                        <h2 style="color: #111827;">Gói của bạn sắp hết hạn</h2>
                        <p style="color: #374151;">
                            Gói <strong>%s</strong> của bạn đã hết hạn. Bạn sẽ còn
                            <strong style="color: %s;">%s</strong> để gia hạn.
                            Hãy gia hạn để không bị gián đoạn dịch vụ.
                        </p>

                        <div style="margin: 24px 0; padding: 16px; background: #fff7ed;
                                    border-left: 4px solid %s; border-radius: 8px;">
                            <p style="margin: 0; color: #92400e;">
                                📦 Gói hiện tại: <strong>%s</strong><br/>
                                ⏳ Thời hạn còn lại: <strong style="color: %s;">%s</strong>
                            </p>
                        </div>

                        %s

                        <a href="%s/me/subscriptions"
                           style="display: inline-block; padding: 12px 24px; background: #023c3d;
                                  color: #ffffff; text-decoration: none; border-radius: 8px; font-weight: bold;">
                            Gia hạn ngay
                        </a>

                        <hr style="border: none; border-top: 1px solid #e5e7eb; margin: 24px 0 16px;"/>
                        <p style="margin: 0; font-size: 12px; color: #9ca3af; text-align: center;">
                            Đây là email tự động từ Synkork — vui lòng không reply.
                        </p>
                    </div>
                """.formatted(plan, urgencyColor, urgencyText, urgencyColor, plan, urgencyColor, urgencyText,
                deletionBlock, frontendUrl);

        emailService.send(toEmail, subject, body);
    }

    @Async
    public void sendPlanExpiredEmail(String toEmail) {
        String subject = "[Synkork] Gói đăng ký của bạn đã hết hạn";

        String body = """
                    <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;
                                padding: 24px; border: 1px solid #e5e7eb; border-radius: 12px;">

                        <h2 style="color: #111827;">Gói của bạn đã hết hạn</h2>

                        <p style="color: #374151;">
                            Gói đăng ký của bạn đã hết hạn. Tài khoản của bạn đã được
                            chuyển về gói <strong>Free</strong>.
                        </p>

                        <div style="margin: 24px 0; padding: 16px; background: #fef2f2;
                                    border-left: 4px solid #ef4444; border-radius: 8px;">
                            <p style="margin: 0; color: #991b1b;">
                                ⚠️ Các phòng và kênh vượt quá giới hạn gói Free đã bị xóa.<br/>
                                📦 Gói hiện tại: <strong>Free</strong>
                            </p>
                        </div>

                        <a href="%s/me/subscriptions"
                           style="display: inline-block; padding: 12px 24px; background: #023c3d;
                                  color: #ffffff; text-decoration: none; border-radius: 8px; font-weight: bold;">
                            Nâng cấp lại ngay
                        </a>

                        <hr style="border: none; border-top: 1px solid #e5e7eb; margin: 24px 0 16px;"/>
                        <p style="margin: 0; font-size: 12px; color: #9ca3af; text-align: center;">
                            Đây là email tự động từ Synkork — vui lòng không reply.
                        </p>
                    </div>
                """.formatted(frontendUrl);

        emailService.send(toEmail, subject, body);
    }
}
