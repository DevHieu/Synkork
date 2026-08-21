package com.synkork.backend.modules.collaboration.note.utils;

import com.synkork.backend.common.utils.EmailService;
import com.synkork.backend.modules.collaboration.note.NoteEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Service
public class NoteEmail {

    @Autowired
    private EmailService emailService;

    @Async
    public void sendNoteReminderEmail(NoteEntity note) {
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
                + note.getNote() + "</p>"
                : "";

        String body = """
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
                """
                .formatted(note.getTitle(), noteContent, timeStr);

        emailService.send(toEmail, "🔔 Nhắc nhở: " + note.getTitle(), body);
    }
}
