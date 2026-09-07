package com.synkork.backend.modules.collaboration.task.card;

import java.util.ArrayList;
import java.util.List;

import com.synkork.backend.modules.collaboration.task.utils.TaskEmail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.synkork.backend.modules.collaboration.task.card.enums.CardStatus;
import com.synkork.backend.modules.notification.NotificationService;
import com.synkork.backend.modules.notification.enums.NotificationRefTypeEnum;
import com.synkork.backend.modules.notification.enums.NotificationTypeEnum;
import com.synkork.backend.modules.roomMember.RoomMemberEntity;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CardDeadlineScheduler {

    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private TaskEmail taskEmail;

    @Autowired
    private NotificationService notificationService;

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void checkDeadline() {

        List<CardEntity> cards = cardRepository.findAllWithAssignees();

        List<CardEntity> overdueCards = new ArrayList<>();
        List<CardEntity> dueSoonCards = new ArrayList<>();

        for (CardEntity card : cards) {

            if (card.getDueDate() == null)
                continue;

            CardStatus status = card.getStatus();

            if (status == CardStatus.OVERDUE &&
                    !Boolean.TRUE.equals(card.getOverdueMailSent())) {

                overdueCards.add(card);
                card.setOverdueMailSent(true);

                for (RoomMemberEntity member : card.getAssignees()) {

                    notificationService.sendNotification(
                            null,
                            member.getUser(),
                            card.getId(),
                            card.getColumn().getSpace().getRoom().getId(),
                            card.getColumn().getSpace().getId(),
                            NotificationTypeEnum.TASK,
                            NotificationRefTypeEnum.CARD_OVER_DUE
                        );
                            
                }
            }

            if (status == CardStatus.DUE_SOON &&
                    !Boolean.TRUE.equals(card.getDueSoonMailSent())) {

                dueSoonCards.add(card);
                card.setDueSoonMailSent(true);

                for (RoomMemberEntity member : card.getAssignees()) {

                    notificationService.sendNotification(
                            null,
                            member.getUser(),
                            card.getId(),
                            card.getColumn().getSpace().getRoom().getId(),
                            card.getColumn().getSpace().getId(),
                            NotificationTypeEnum.TASK,
                            NotificationRefTypeEnum.CARD_DUE_SOON
                        );
                }
            }
        }

        if (!overdueCards.isEmpty()) {
            taskEmail.sendOverdueSummaryMail(overdueCards);
        }

        if (!dueSoonCards.isEmpty()) {
            taskEmail.sendDueSoonSummaryMail(dueSoonCards);
        }

        cardRepository.saveAll(cards);
    }
}