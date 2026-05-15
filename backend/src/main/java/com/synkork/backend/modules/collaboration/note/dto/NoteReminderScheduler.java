package com.synkork.backend.modules.collaboration.note.dto;

import com.synkork.backend.modules.collaboration.note.NoteEntity;
import com.synkork.backend.modules.collaboration.note.NoteService;
import com.synkork.backend.modules.collaboration.note.dto.NoteResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import jakarta.mail.internet.MimeMessage;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
public class NoteReminderScheduler {

    @Autowired
    private NoteService noteService;

    @Autowired
    private SimpMessagingTemplate messageTemplate;

    @Autowired
    private JavaMailSender mailSender;

    @Value("${gmail.username}")
    private String fromEmail;

    @Scheduled(fixedDelay = 30000)
    public void checkAndSendReminders() {
        System.out.println("⏰ Scheduler running at: " + Instant.now());

        List<NoteEntity> pending = noteService.getPendingReminders();

        System.out.println("📋 Pending reminders: " + pending.size());

        for (NoteEntity note : pending) {
            System.out.println("🔔 Firing reminder for note: " + note.getId());

            // 1. Gửi email
            sendReminderEmail(note);

            // 2. Gửi socket để update UI (icon chuông tắt)
            NoteResponse response = new NoteResponse(note);
            String spaceId = note.getSpace().getId().toString();
            messageTemplate.convertAndSend(
                "/topic/space/" + spaceId + "/notes/reminder",
                response
            );

            // 3. Đánh dấu đã gửi
            noteService.markReminderSent(note);
        }
    }

    private void sendReminderEmail(NoteEntity note) {
        try {
            String toEmail = note.getCreatedBy().getEmail();

            if (toEmail == null || toEmail.isBlank()) {
                System.err.println("⚠️ No email for user: " + note.getCreatedBy().getId());
                return;
            }

            DateTimeFormatter formatter = DateTimeFormatter
                .ofPattern("HH:mm — dd/MM/yyyy")
                .withZone(ZoneId.of("Asia/Ho_Chi_Minh"));

            String timeStr = note.getReminderAt() != null
                ? formatter.format(note.getReminderAt())
                : "";

            String noteContent = note.getNote() != null && !note.getNote().isBlank()
                ? "<p style=\"margin: 0; font-size: 14px; color: #374151; white-space: pre-wrap;\">"
                    + note.getNote()
                    + "</p>"
                : "";

            String html = """
                <div style="font-family: Arial, sans-serif; max-width: 480px; margin: 0 auto; padding: 24px; border: 1px solid #e5e7eb; border-radius: 12px; background: #ffffff;">

                    <div style="display: flex; align-items: center; gap: 8px; margin-bottom: 20px;">
                        <span style="font-size: 28px;">🔔</span>
                        <h2 style="margin: 0; font-size: 20px; color: #111827;">Nhắc nhở của bạn</h2>
                    </div>

                    <div style="background: #f9fafb; border-radius: 8px; padding: 16px; margin-bottom: 16px; border-left: 4px solid #f97316;">
                        <h3 style="margin: 0 0 8px 0; font-size: 16px; color: #111827;">%s</h3>
                        %s
                    </div>

                    <p style="margin: 0 0 20px 0; font-size: 13px; color: #6b7280;">
                        ⏰ Thời gian nhắc: <strong style="color: #111827;">%s</strong>
                    </p>

                    <hr style="border: none; border-top: 1px solid #e5e7eb; margin-bottom: 16px;" />

                    <p style="margin: 0; font-size: 12px; color: #9ca3af; text-align: center;">
                        Đây là email tự động từ Synkork — vui lòng không reply.
                    </p>
                </div>
            """.formatted(note.getTitle(), noteContent, timeStr);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("🔔 Nhắc nhở: " + note.getTitle());
            helper.setText(html, true);

            mailSender.send(message);

            System.out.println("✅ Email sent to: " + toEmail + " for note: " + note.getId());

        } catch (Exception e) {
            System.err.println("❌ Failed to send email for note: " + note.getId() + " — " + e.getMessage());
        }
    }
}