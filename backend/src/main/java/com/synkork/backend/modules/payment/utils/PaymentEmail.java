package com.synkork.backend.modules.payment.utils;

import com.synkork.backend.common.utils.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class PaymentEmail {

    @Autowired
    private EmailService emailService;

    @Async
    public void sendPaymentSuccessEmail(String toEmail, String plan) {
        String subject = "✅ [Synkork] Thanh toán VIP thành công";

        String body = """
                    <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;
                                padding: 24px; border: 1px solid #e5e7eb; border-radius: 12px;">
                        <h2 style="color: #023c3d;">🎉 Gói VIP đã được kích hoạt!</h2>
                        <p>Cảm ơn bạn đã nâng cấp lên gói <b>%s</b>.</p>
                        <div style="margin: 24px 0; padding: 16px; background: #f0fdf4;
                                    border-left: 4px solid #22c55e; border-radius: 8px;">
                            <p style="margin: 0; color: #166534;">
                                ✔ Gói: <strong>%s</strong><br/>
                                ✔ Trạng thái: Đã kích hoạt
                            </p>
                        </div>
                        <p style="color: #888; font-size: 13px;">Nếu có thắc mắc, liên hệ đội ngũ hỗ trợ Synkork.</p>
                        <hr style="border: none; border-top: 1px solid #e5e7eb; margin: 16px 0;"/>
                        <p style="margin: 0; font-size: 12px; color: #9ca3af; text-align: center;">
                            Đây là email tự động từ Synkork — vui lòng không reply.
                        </p>
                    </div>
                """.formatted(plan, plan);

        emailService.send(toEmail, subject, body);
    }
}
