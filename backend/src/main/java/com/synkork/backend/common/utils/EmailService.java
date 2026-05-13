package com.synkork.backend.common.utils;

import jakarta.mail.Message;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.synkork.backend.modules.collaboration.task.card.CardEntity;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class EmailService {

    @Value("${frontend.client.url}")
    private String frontendUrl;

    @Value("${gmail.username}")
    private String username;

    @Value("${gmail.password}")
    private String password;

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

        send(to, subject, body);
    }

    @Async
    public void sendForgotPasswordEmail(String to, String verificationId) {
        String resetLink = frontendUrl + "/auth/reset-password?token=" + verificationId;

        String subject = "[Synkork] Đặt lại mật khẩu";

        String body = """
                <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;">
                    <h2 style="color: #023c3d;">Đặt lại mật khẩu</h2>
                    <p>Chúng tôi nhận được yêu cầu đặt lại mật khẩu cho tài khoản của bạn.</p>
                    <a href="%s"
                       style="display: inline-block; padding: 12px 24px; background-color: #023c3d;
                              color: white; text-decoration: none; border-radius: 6px; margin: 16px 0;">
                        Đặt lại mật khẩu
                    </a>
                    <p style="color: #888; font-size: 13px;">Link có hiệu lực trong 15 phút.</p>
                    <p style="color: #888; font-size: 13px;">Nếu bạn không yêu cầu đặt lại mật khẩu, hãy bỏ qua email này.</p>
                </div>
                """
                .formatted(resetLink);

        send(to, subject, body);
    }

    public boolean send(String to, String subject, String body) {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.ssl.protocols", "TLSv1.2");

        Session session = Session.getInstance(props, new jakarta.mail.Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        try {
            MimeMessage mail = new MimeMessage(session);
            mail.setFrom(new InternetAddress(username)); // from = username
            mail.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            mail.setSubject(subject, "utf-8");
            mail.setText(body, "utf-8", "html");
            Transport.send(mail);

            System.out.println("Gửi mail thành công!");
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public void sendDueSoonSummaryMail(List<CardEntity> cards) {

        if (cards.isEmpty())
            return;

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy");

        // lấy email không trùng
        Set<String> emails = cards.stream()
                .flatMap(card -> card.getAssignees().stream())
                .map(member -> member.getUser().getEmail())
                .collect(Collectors.toSet());

        // render danh sách task
        String items = cards.stream()
                .map(card -> """
                        <li style="margin-bottom: 8px;">
                            <b>%s</b><br/>
                            <span style="color:#666;">
                                Hạn chót: %s
                            </span>
                        </li>
                        """.formatted(
                        card.getTitle(),
                        card.getDueDate().format(formatter)))
                .collect(Collectors.joining());

        String subject = "[Synkork] Các thẻ sắp đến hạn";

        String body = """
                <div style="font-family: Arial; padding:16px;">

                    <h2 style="color: #d97706;">
                        🟡 Các thẻ sắp đến hạn
                    </h2>

                    <p>
                        Bạn có <b>%d</b> thẻ sắp đến hạn trong vòng 24 giờ:
                    </p>

                    <ul style="padding-left:20px;">
                        %s
                    </ul>

                    <p style="margin-top:16px;">
                        Vui lòng hoàn thành sớm.
                    </p>

                </div>
                """.formatted(cards.size(), items);

        for (String email : emails) {
            send(email, subject, body);
        }
    }

    public void sendOverdueSummaryMail(List<CardEntity> cards) {

        if (cards.isEmpty())
            return;

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy");

        // lấy toàn bộ email không trùng
        Set<String> emails = cards.stream()
                .flatMap(card -> card.getAssignees().stream())
                .map(member -> member.getUser().getEmail())
                .collect(Collectors.toSet());

        // render danh sách card
        String items = cards.stream()
                .map(card -> """
                        <li style="margin-bottom: 8px;">
                            <b>%s</b><br/>
                            <span style="color:#666;">
                                Hết hạn: %s
                            </span>
                        </li>
                        """.formatted(
                        card.getTitle(),
                        card.getDueDate().format(formatter)))
                .collect(Collectors.joining());

        String subject = "[Synkork] Các thẻ đã quá hạn";

        String body = """
                <div style="font-family: Arial; padding:16px;">

                    <h2 style="color: #dc2626;">
                        🔴 Các thẻ đã quá hạn
                    </h2>

                    <p>
                        Bạn có <b>%d</b> thẻ đã quá hạn:
                    </p>

                    <ul style="padding-left:20px;">
                        %s
                    </ul>

                </div>
                """.formatted(cards.size(), items);

        for (String email : emails) {
            send(email, subject, body);
        }
    }

}
