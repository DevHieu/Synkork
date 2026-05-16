package com.synkork.backend.modules.collaboration.task.card;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.synkork.backend.modules.collaboration.task.column.ColumnEntity;
import com.synkork.backend.modules.collaboration.task.column.ColumnRepository;
import com.synkork.backend.modules.collaboration.task.dto.CardDTO;
import com.synkork.backend.modules.collaboration.task.dto.CardMovePayload;
import com.synkork.backend.modules.collaboration.task.dto.CardRequest;
import com.synkork.backend.modules.collaboration.task.dto.MoveCardRequest;
import com.synkork.backend.modules.roomMember.RoomMemberEntity;
import com.synkork.backend.modules.roomMember.RoomMemberRepository;

import jakarta.transaction.Transactional;

@Service
public class CardService {

    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private ColumnRepository columnRepository;

    @Autowired
    private RoomMemberRepository roomMemberRepository;

    @Transactional
    public CardDTO createCard(UUID spaceId, String creatorEmail, CardRequest req) {
        UUID columnId = req.getColumnId();
        ColumnEntity column = columnRepository.findById(columnId)
                .orElseThrow(() -> new RuntimeException("Cột không tồn tại"));
            
        int nextPosition = cardRepository.findByColumn_IdOrderByPositionAsc(columnId).size();

        CardEntity card = new CardEntity();
        card.setColumn(column);
        card.setTitle(req.getTitle());
        card.setPosition(nextPosition);
        card.setDescription(req.getDescription() != null ? req.getDescription() : "");
        card.setCreatedAt(LocalDateTime.now());

        UUID roomId = column.getSpace().getRoom().getId();
        card.setCreatedBy(
            roomMemberRepository.findByUser_EmailAndRoom_Id(creatorEmail, roomId)
                .orElseThrow(() -> new RuntimeException("User không phải member của room này"))
        );

        if (req.getAssigneeIds() != null && !req.getAssigneeIds().isEmpty()) {
            List<RoomMemberEntity> assignees = roomMemberRepository.findAllById(req.getAssigneeIds());
            card.setAssignees(assignees);
        }

        card.setDueDate(req.getDueDate());

        CardEntity savedCard = cardRepository.save(card);
        return new CardDTO(savedCard);
    }

    @Transactional
    public CardDTO updateCard(UUID cardId, CardRequest req) {
        CardEntity card = cardRepository.findById(cardId)
                .orElseThrow(() -> new RuntimeException("Card không tồn tại"));

        if(req.getTitle() != null){
            card.setTitle(req.getTitle());
        }

        if(req.getDescription() != null){
            card.setDescription(req.getDescription());
        }

        card.setDueDate(req.getDueDate());
        
        System.out.println("assigneeIds từ request: " + req.getAssigneeIds());
        System.out.println("isEmpty check: " + (req.getAssigneeIds() != null && !req.getAssigneeIds().isEmpty()));

        if (req.getAssigneeIds() != null ) {
            List<RoomMemberEntity> assignees = roomMemberRepository.findAllById(req.getAssigneeIds());
            System.out.println("Users tìm được: " + assignees.stream().map(u -> u.getId().toString()).toList());
            card.setAssignees(assignees);
        }

        

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

    @Transactional
    public CardDTO getCardById(UUID cardUUID) {
        CardEntity card = cardRepository.findById(cardUUID)
                .orElseThrow(() -> new RuntimeException("Card không tồn tại"));
        return new CardDTO(card);
    }

    @Transactional
    public CardMovePayload moveCard(UUID cardId, MoveCardRequest req) {
        CardEntity card = cardRepository.findById(cardId)
            .orElseThrow(() -> new RuntimeException("Card không tồn tại"));
            
        ColumnEntity oldCol = card.getColumn();
        ColumnEntity newCol = columnRepository.findById(req.getTargetColumnId())
            .orElseThrow(() -> new RuntimeException("Cột đích không tồn tại"));

        boolean isSameColumn = oldCol.getId().equals(req.getTargetColumnId());

        if (isSameColumn) {
            handleSameColumnMove(newCol, card, req.getNewPosition());
        } else {
            handleCrossColumnMove(oldCol, newCol, card, req.getNewPosition());
        }

        card.setColumn(newCol);
        card.setPosition(req.getNewPosition());
        cardRepository.save(card);
        cardRepository.flush();

        List<CardDTO> targetCards = cardRepository
            .findByColumn_IdOrderByPositionAsc(newCol.getId())
            .stream().map(CardDTO::new).toList();

        List<CardDTO> sourceCards = null;
        UUID sourceColumnId = null;
        if (!isSameColumn) {
            sourceColumnId = oldCol.getId();
            sourceCards = cardRepository
                .findByColumn_IdOrderByPositionAsc(oldCol.getId())
                .stream().map(CardDTO::new).toList();
        }

        return new CardMovePayload(newCol.getId(), sourceColumnId, targetCards, sourceCards);
    }

    private void handleSameColumnMove(ColumnEntity column, CardEntity card, int newPosition) {
        int oldPosition = card.getPosition();
        List<CardEntity> cards = cardRepository.findByColumn_IdOrderByPositionAsc(column.getId())
            .stream()
            .filter(c -> !c.getId().equals(card.getId()))
            .collect(Collectors.toList());

        if (oldPosition < newPosition) {
            for (CardEntity c : cards) {
                if (c.getPosition() > oldPosition && c.getPosition() <= newPosition) {
                    c.setPosition(c.getPosition() - 1);
                }
            }
        } else if (oldPosition > newPosition) {
            for (CardEntity c : cards) {
                if (c.getPosition() >= newPosition && c.getPosition() < oldPosition) {
                    c.setPosition(c.getPosition() + 1);
                }
            }
        }
        cardRepository.saveAll(cards);
    }

    private void handleCrossColumnMove(ColumnEntity oldCol, ColumnEntity newCol, CardEntity card, int newPosition) {
        List<CardEntity> oldCards = cardRepository.findByColumn_IdOrderByPositionAsc(oldCol.getId())
            .stream()
            .filter(c -> !c.getId().equals(card.getId()))
            .collect(Collectors.toList());
        for (CardEntity c : oldCards) {
            if (c.getPosition() > card.getPosition()) {
                c.setPosition(c.getPosition() - 1);
            }
        }
        cardRepository.saveAll(oldCards);

        List<CardEntity> newCards = cardRepository.findByColumn_IdOrderByPositionAsc(newCol.getId());
        for (CardEntity c : newCards) {
            if (c.getPosition() >= newPosition) {
                c.setPosition(c.getPosition() + 1);
            }
        }
        cardRepository.saveAll(newCards);
    }
}
