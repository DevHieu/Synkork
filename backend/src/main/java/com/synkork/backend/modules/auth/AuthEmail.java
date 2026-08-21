package com.synkork.backend.modules.auth;

import com.synkork.backend.common.utils.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class AuthEmail {

    @Value("${frontend.client.url}")
    private String frontendUrl;

    @Autowired
    private EmailService emailService;

    @Async
    public void sendVerificationEmail(String to, String verificationId) {
        String verifyLink = frontendUrl + "/auth/verify?token=" + verificationId;

        String subject = "[Synkork] Xác thực tài khoản của bạn";

        String body = """
                <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;">
                    <h2 style="color: #023c3d;">Chào mừng đến với Synkork!</h2>
                    <p>Cảm ơn bạn đã đăng ký. Vui lòng click vào nút bên dưới để xác thực tài khoản.</p>
                    <a href="%s"
                       style="display: inline-block; padding: 12px 24px; background-color: #023c3d;
                              color: white; text-decoration: none; border-radius: 6px; margin: 16px 0;">
                        Xác thực tài khoản
                    </a>
                    <p style="color: #888; font-size: 13px;">Link có hiệu lực trong 5 phút.</p>
                    <p style="color: #888; font-size: 13px;">Nếu bạn không đăng ký tài khoản, hãy bỏ qua email này.</p>
                </div>
                """.formatted(verifyLink);

        emailService.send(to, subject, body);
    }

    @Async
    public void sendOTPEmail(String to, String otp) {
        String subject = "[Synkork] Mã OTP xác thực của bạn";

        String body = """
                <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;">
                    <h2 style="color: #023c3d;">Xác thực tài khoản Synkork</h2>
                    <p>Mã OTP của bạn là:</p>
                    <div style="font-size: 32px; font-weight: bold; letter-spacing: 8px;
                                color: #023c3d; margin: 24px 0; text-align: center;">
                        %s
                    </div>
                    <p style="color: #888; font-size: 13px;">Mã có hiệu lực trong 5 phút.</p>
                    <p style="color: #888; font-size: 13px;">Nếu bạn không yêu cầu mã này, hãy bỏ qua email.</p>
                </div>
                """.formatted(otp);

        emailService.send(to, subject, body);
    }
}
