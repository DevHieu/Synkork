package com.synkork.backend.modules.collaboration.note;

import com.synkork.backend.common.utils.EmailService;
import com.synkork.backend.modules.notification.NotificationService;
import com.synkork.backend.modules.notification.enums.NotificationRefTypeEnum;
import com.synkork.backend.modules.notification.enums.NotificationTypeEnum;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class NoteReminderScheduler {

    @Autowired
    private NoteService noteService;

    @Autowired
    private SimpMessagingTemplate messageTemplate;

    @Autowired
    private EmailService emailService;

    @Autowired
    private NotificationService notificationService;

    @Scheduled(fixedDelay = 30000)
    public void checkAndSendReminders() {
        List<NoteEntity> pending = noteService.getPendingReminders();

        for (NoteEntity note : pending) {
            emailService.sendNoteReminderEmail(note);

            NoteResponse response = new NoteResponse(note);
            String spaceId = note.getSpace().getId().toString();
            messageTemplate.convertAndSend(
                    "/topic/space/" + spaceId + "/notes/reminder",
                    response
            );

            notificationService.sendNotification(null, note.getCreatedBy(), null, note.getSpace().getRoom().getId(), note.getSpace().getId(), NotificationTypeEnum.NOTE, NotificationRefTypeEnum.NOTE_REMINDER);

            noteService.markReminderSent(note);
        }
    }
}