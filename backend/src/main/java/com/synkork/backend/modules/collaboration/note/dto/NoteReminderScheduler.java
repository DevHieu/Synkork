package com.synkork.backend.modules.collaboration.note.dto;

import com.synkork.backend.common.utils.EmailService;
import com.synkork.backend.modules.collaboration.note.NoteEntity;
import com.synkork.backend.modules.collaboration.note.NoteService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
public class NoteReminderScheduler {

    @Autowired
    private NoteService noteService;

    @Autowired
    private SimpMessagingTemplate messageTemplate;

    @Autowired
    private EmailService emailService;

    @Scheduled(fixedDelay = 30000)
    public void checkAndSendReminders() {
        System.out.println("⏰ Scheduler running at: " + Instant.now());

        List<NoteEntity> pending = noteService.getPendingReminders();
        System.out.println("📋 Pending reminders: " + pending.size());

        for (NoteEntity note : pending) {
            System.out.println("🔔 Firing reminder for note: " + note.getId());

            emailService.sendNoteReminderEmail(note);

            NoteResponse response = new NoteResponse(note);
            String spaceId = note.getSpace().getId().toString();
            messageTemplate.convertAndSend(
                    "/topic/space/" + spaceId + "/notes/reminder",
                    response
            );

            noteService.markReminderSent(note);
        }
    }
}