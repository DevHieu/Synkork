package com.synkork.backend.modules.collaboration.task.card;

import java.util.ArrayList;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.synkork.backend.common.utils.EmailService;
import com.synkork.backend.modules.collaboration.task.card.enums.CardStatus;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CardDeadlineScheduler {

    private final CardRepository cardRepository;
    private final EmailService emailService;

    @Scheduled(fixedRate = 10000)
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
            }

            if (status == CardStatus.DUE_SOON &&
                    !Boolean.TRUE.equals(card.getDueSoonMailSent())) {

                dueSoonCards.add(card);
                card.setDueSoonMailSent(true);
            }
        }

        if (!overdueCards.isEmpty()) {
            emailService.sendOverdueSummaryMail(overdueCards);
        }

        if (!dueSoonCards.isEmpty()) {
            emailService.sendDueSoonSummaryMail(dueSoonCards);
        }

        cardRepository.saveAll(cards);
    }
}