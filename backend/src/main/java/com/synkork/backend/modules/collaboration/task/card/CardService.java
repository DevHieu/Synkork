package com.synkork.backend.modules.collaboration.task.card;

import java.util.List;
import java.util.UUID;

import javax.smartcardio.Card;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.synkork.backend.modules.collaboration.task.column.ColumnEntity;
import com.synkork.backend.modules.collaboration.task.column.ColumnRepository;
import com.synkork.backend.modules.collaboration.task.dto.CardDTO;
import com.synkork.backend.modules.collaboration.task.dto.CardRequest;
import com.synkork.backend.modules.collaboration.task.dto.MoveCardRequest;
import com.synkork.backend.modules.user.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
public class CardService {

    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ColumnRepository columnRepository;

    @Transactional
    public CardDTO createCard(UUID columnId, String creatorEmail, CardRequest req) {
        ColumnEntity column = columnRepository.findById(columnId)
                .orElseThrow(() -> new RuntimeException("Cột không tồn tại"));

        // Cách lấy position ngắn gọn hơn: đếm số lượng card hiện có
        int nextPosition = cardRepository.findByColumn_IdOrderByPositionAsc(columnId).size();

        CardEntity card = new CardEntity();
        card.setColumn(column);
        card.setTitle(req.getTitle());
        card.setPosition(nextPosition);
        card.setDescription(req.getDescription() != null ? req.getDescription() : "");

        card.setCreatedBy(userRepository.findByEmail(creatorEmail)
                    .orElseThrow(() -> new RuntimeException("User không tồn tại")));


        CardEntity savedCard = cardRepository.save(card);
        return new CardDTO(savedCard);
    }

    @Transactional
    public CardDTO updateCard(UUID cardId, CardRequest req) {
        CardEntity card = cardRepository.findById(cardId)
                .orElseThrow(() -> new RuntimeException("Card không tồn tại"));

        card.setTitle(req.getTitle());
        card.setDescription(req.getDescription() != null ? req.getDescription() : "");

        CardEntity updatedCard = cardRepository.save(card);
        return new CardDTO(updatedCard);
    }

    @Transactional
    public void deleteCard(UUID cardId) {
        CardEntity card = cardRepository.findById(cardId)
                .orElseThrow(() -> new RuntimeException("Card không tồn tại"));

        UUID columnId = card.getColumn().getId();
        int deletedPos = card.getPosition();

        cardRepository.delete(card);

        List<CardEntity> remainingCards = cardRepository.findByColumn_IdOrderByPositionAsc(columnId);
        for (CardEntity c : remainingCards) {
            if (c.getPosition() > deletedPos) {
                c.setPosition(c.getPosition() - 1);
                cardRepository.save(c);
            }
        }
    }

    public CardDTO getCardById(UUID cardUUID) {
        CardEntity card = cardRepository.findById(cardUUID)
                .orElseThrow(() -> new RuntimeException("Card không tồn tại"));
        return new CardDTO(card);
    }

    @Transactional
    public void moveCard(UUID cardId, MoveCardRequest req) {

        CardEntity card = cardRepository.findById(cardId)
            .orElseThrow(() -> new RuntimeException("Card không tồn tại"));
            
        ColumnEntity oldCol = card.getColumn();
        ColumnEntity newCol = columnRepository.findById(cardId)
        .orElseThrow(() -> new RuntimeException("Cột đích không tồn tại"));

        if(oldCol.getId().equals(req.getTargetColumnId())){
            handleSameColumnMove(newCol, card, req.getNewPosition());
        } else {
            handleCrossColumnMove(oldCol, newCol, card, req.getNewPosition());
        }

        card.setColumn(newCol);
        card.setPosition(req.getNewPosition());

        cardRepository.save(card);
    }

    // CASE 1: Di chuyển trong cùng một cột
    private void handleSameColumnMove(ColumnEntity column, CardEntity card, int newPosition) {
        int oldPosition = card.getPosition();
        List<CardEntity> cards = cardRepository.findByColumn_IdOrderByPositionAsc(column.getId());

        if (oldPosition < newPosition) {
            // Kéo xuống: Các card ở giữa sẽ bị giảm vị trí đi 1
            for (CardEntity c : cards) {
                if (c.getPosition() > oldPosition && c.getPosition() <= newPosition) {
                    c.setPosition(c.getPosition() - 1);
                }
            }
        } else if (oldPosition > newPosition) {
            // Kéo lên: Các card ở giữa sẽ bị tăng vị trí lên 1
            for (CardEntity c : cards) {
                if (c.getPosition() >= newPosition && c.getPosition() < oldPosition) {
                    c.setPosition(c.getPosition() + 1);
                }
            }
        }
        cardRepository.saveAll(cards);
    }

    // CASE 2: Di chuyển sang cột khác
    private void handleCrossColumnMove(ColumnEntity oldCol, ColumnEntity newCol, CardEntity card, int newPosition) {
        // 1. Cập nhật các card ở cột cũ (lấp chỗ trống)
        List<CardEntity> oldCards = cardRepository.findByColumn_IdOrderByPositionAsc(oldCol.getId());
        for (CardEntity c : oldCards) {
            if (c.getPosition() > card.getPosition()) {
                c.setPosition(c.getPosition() - 1);
            }
        }
        cardRepository.saveAll(oldCards);

        // 2. Cập nhật các card ở cột mới (nhường chỗ cho card mới tới)
        List<CardEntity> newCards = cardRepository.findByColumn_IdOrderByPositionAsc(newCol.getId());
        for (CardEntity c : newCards) {
            if (c.getPosition() >= newPosition) {
                c.setPosition(c.getPosition() + 1);
            }
        }
        cardRepository.saveAll(newCards);
    }
    // public UUID getBoardIdByColumnId(UUID columnId) {
    //     return columnRepository.findBoardIdByColumnId(columnId)
    //             .orElseThrow(() -> new RuntimeException("Column not found"));
    // }

    // public UUID getBoardIdByTaskId(UUID taskId) {
    //     return cardRepository.findBoardIdByTaskId(taskId)
    //             .orElseThrow(() -> new RuntimeException("Task not found"));
    // }
}
