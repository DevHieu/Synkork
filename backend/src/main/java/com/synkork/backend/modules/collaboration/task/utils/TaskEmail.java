package com.synkork.backend.modules.collaboration.task.utils;

import com.synkork.backend.common.utils.EmailService;
import com.synkork.backend.modules.collaboration.task.card.CardEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class TaskEmail {

    @Autowired
    private EmailService emailService;

    @Async
    public void sendDueSoonSummaryMail(List<CardEntity> cards) {

        if (cards.isEmpty())
            return;

        DateTimeFormatter formatter = DateTimeFormatter
                .ofPattern("HH:mm dd/MM/yyyy")
                .withZone(ZoneId.of("Asia/Ho_Chi_Minh"));

        Set<String> emails = cards.stream()
                .flatMap(card -> card.getAssignees().stream())
                .map(member -> member.getUser().getEmail())
                .collect(Collectors.toSet());

        String items = cards.stream()
                .map(card -> """
                        <li style="margin-bottom: 10px; padding-bottom: 10px; border-bottom: 1px solid #fde68a; list-style: none;">
                            <div style="font-weight: 600; color: #111827; font-size: 14px;">%s</div>
                            <div style="color: #92400e; font-size: 13px; margin-top: 2px;">
                                ⏰ Hạn chót: %s
                            </div>
                        </li>
                        """
                        .formatted(
                                card.getTitle(),
                                card.getDueDate().format(formatter)))
                .collect(Collectors.joining());

        String subject = "[Synkork] Các thẻ sắp đến hạn";

        String body = """
                <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;
                            padding: 24px; border: 1px solid #e5e7eb; border-radius: 12px;">

                    <h2 style="margin: 0 0 8px 0; color: #111827;">🟡 Các thẻ sắp đến hạn</h2>

                    <p style="color: #374151;">
                        Bạn có <strong style="color: #d97706;">%d</strong> thẻ sắp đến hạn trong vòng 24 giờ:
                    </p>

                    <div style="margin: 16px 0; padding: 16px; background: #fffbeb;
                                border-left: 4px solid #f59e0b; border-radius: 8px;">
                        <ul style="margin: 0; padding: 0;">
                            %s
                        </ul>
                    </div>

                    <p style="color: #374151; margin: 16px 0 0 0;">
                        Vui lòng hoàn thành sớm để tránh bị trễ hạn.
                    </p>

                    <hr style="border: none; border-top: 1px solid #e5e7eb; margin: 24px 0 16px;"/>
                    <p style="margin: 0; font-size: 12px; color: #9ca3af; text-align: center;">
                        Đây là email tự động từ Synkork — vui lòng không reply.
                    </p>
                </div>
                """.formatted(cards.size(), items);

        for (String email : emails) {
            emailService.send(email, subject, body);
        }
    }

    @Async
    public void sendOverdueSummaryMail(List<CardEntity> cards) {

        if (cards.isEmpty())
            return;

        DateTimeFormatter formatter = DateTimeFormatter
                .ofPattern("HH:mm dd/MM/yyyy")
                .withZone(ZoneId.of("Asia/Ho_Chi_Minh"));

        Set<String> emails = cards.stream()
                .flatMap(card -> card.getAssignees().stream())
                .map(member -> member.getUser().getEmail())
                .collect(Collectors.toSet());

        String items = cards.stream()
                .map(card -> """
                        <li style="margin-bottom: 10px; padding-bottom: 10px; border-bottom: 1px solid #fecaca; list-style: none;">
                            <div style="font-weight: 600; color: #111827; font-size: 14px;">%s</div>
                            <div style="color: #991b1b; font-size: 13px; margin-top: 2px;">
                                ⚠️ Hết hạn: %s
                            </div>
                        </li>
                        """
                        .formatted(
                                card.getTitle(),
                                card.getDueDate().format(formatter)))
                .collect(Collectors.joining());

        String subject = "[Synkork] Các thẻ đã quá hạn";

        String body = """
                <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;
                            padding: 24px; border: 1px solid #e5e7eb; border-radius: 12px;">

                    <h2 style="margin: 0 0 8px 0; color: #111827;">🔴 Các thẻ đã quá hạn</h2>

                    <p style="color: #374151;">
                        Bạn có <strong style="color: #dc2626;">%d</strong> thẻ đã quá hạn cần xử lý:
                    </p>

                    <div style="margin: 16px 0; padding: 16px; background: #fef2f2;
                                border-left: 4px solid #ef4444; border-radius: 8px;">
                        <ul style="margin: 0; padding: 0;">
                            %s
                        </ul>
                    </div>

                    <p style="color: #374151; margin: 16px 0 0 0;">
                        Hãy cập nhật trạng thái hoặc gia hạn các thẻ này càng sớm càng tốt.
                    </p>

                    <hr style="border: none; border-top: 1px solid #e5e7eb; margin: 24px 0 16px;"/>
                    <p style="margin: 0; font-size: 12px; color: #9ca3af; text-align: center;">
                        Đây là email tự động từ Synkork — vui lòng không reply.
                    </p>
                </div>
                """.formatted(cards.size(), items);

        for (String email : emails) {
            emailService.send(email, subject, body);
        }
    }

}
