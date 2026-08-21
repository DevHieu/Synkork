package com.synkork.backend.common.utils;

import com.synkork.backend.modules.collaboration.note.NoteEntity;
import com.synkork.backend.modules.user.enums.PlanEnum;
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

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class EmailService {

    @Value("${gmail.username}")
    private String username;

    @Value("${gmail.password}")
    private String password;

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
}
