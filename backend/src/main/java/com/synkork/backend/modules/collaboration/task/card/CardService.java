package com.synkork.backend.modules.collaboration.task.card;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.synkork.backend.modules.collaboration.task.column.ColumnEntity;
import com.synkork.backend.modules.collaboration.task.column.ColumnRepository;
import com.synkork.backend.modules.collaboration.task.dto.CardDTO;
import com.synkork.backend.modules.collaboration.task.dto.CardMovePayload;
import com.synkork.backend.modules.collaboration.task.dto.CardRequest;
import com.synkork.backend.modules.collaboration.task.dto.MoveCardRequest;
import com.synkork.backend.modules.notification.NotificationService;
import com.synkork.backend.modules.notification.enums.NotificationRefTypeEnum;
import com.synkork.backend.modules.notification.enums.NotificationTypeEnum;
import com.synkork.backend.modules.roomMember.RoomMemberEntity;
import com.synkork.backend.modules.roomMember.RoomMemberRepository;
import com.synkork.backend.modules.user.UserEntity;
import com.synkork.backend.modules.user.UserRepository;
import com.synkork.backend.security.UserPrinciple;

import jakarta.transaction.Transactional;

@Service
public class CardService {

    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private ColumnRepository columnRepository;

    @Autowired
    private RoomMemberRepository roomMemberRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationService notificationService;

    @Transactional
    public CardDTO createCard(UUID spaceId, String creatorEmail, CardRequest req) {
        UUID columnId = req.columnId();
        ColumnEntity column = columnRepository.findById(columnId)
                .orElseThrow(() -> new RuntimeException("Cột không tồn tại"));

        int nextPosition = cardRepository.findByColumn_IdAndArchivedFalseOrderByPositionAsc(columnId).size();

        CardEntity card = new CardEntity();
        card.setColumn(column);
        card.setTitle(req.title());
        card.setPosition(nextPosition);
        card.setDescription(req.description() != null ? req.description() : "");
        card.setCreatedAt(LocalDateTime.now());

        UUID roomId = column.getSpace().getRoom().getId();
        card.setCreatedBy(
                roomMemberRepository.findByUser_EmailAndRoom_Id(creatorEmail, roomId)
                        .orElseThrow(() -> new RuntimeException("User không phải member của room này")));

        if (req.assigneeIds() != null && !req.assigneeIds().isEmpty()) {
            List<RoomMemberEntity> assignees = roomMemberRepository.findAllById(req.assigneeIds());
            card.setAssignees(assignees);
        }

        card.setDueDate(req.dueDate());

        CardEntity savedCard = cardRepository.save(card);
        return new CardDTO(savedCard);
    }

    public CardDTO updateCard(UUID cardId, CardRequest req) {
        CardEntity card = findCardById(cardId);

            if (!card.getVersion().equals(req.version())) {
                System.out.println("REQUEST VERSION = " + req.version());
                System.out.println("DB VERSION = " + card.getVersion());
            throw new ObjectOptimisticLockingFailureException(CardEntity.class, card.getId());
        }

        if (req.title() != null) {
            card.setTitle(req.title());
        }

        if (req.description() != null) {
            card.setDescription(req.description());
        }

        card.setDueDate(req.dueDate());

        if(req.completed() != null) card.setCompleted(req.completed());

        if (req.assigneeIds() != null) {
            // Lấy danh sách assignee cũ
            Set<UUID> oldAssigneeIds = card.getAssignees().stream()
                    .map(RoomMemberEntity::getId)
                    .collect(Collectors.toSet());

            List<RoomMemberEntity> newAssignees = roomMemberRepository.findAllById(req.assigneeIds());

            // Tìm người mới được assign (có trong new nhưng không có trong old)
            List<RoomMemberEntity> justAssigned = newAssignees.stream()
                    .filter(member -> !oldAssigneeIds.contains(member.getId()))
                    .toList();

            card.setAssignees(newAssignees);
            CardEntity updatedCard = cardRepository.save(card);

            // Gửi notification cho từng người mới được assign
            if (!justAssigned.isEmpty()) {
                // Lấy actor từ SecurityContext ngay tại đây
                UserPrinciple principal = (UserPrinciple) SecurityContextHolder
                        .getContext().getAuthentication().getPrincipal();
                UserEntity actor = userRepository.findByEmail(principal.getUsername())
                        .orElseThrow(() -> new RuntimeException("Actor không tồn tại"));

                for (RoomMemberEntity member : justAssigned) {
                    // Bỏ qua nếu actor tự assign cho chính mình
                    if (member.getUser().getId().equals(actor.getId()))
                        continue;

                    notificationService.sendNotification(
                            actor,
                            member.getUser(),
                            card.getId(),
                            card.getColumn().getSpace().getRoom().getId(),
                            card.getColumn().getSpace().getId(),
                            NotificationTypeEnum.TASK,
                            NotificationRefTypeEnum.CARD_ASSIGNED
                        );
                }
            }

            return new CardDTO(updatedCard);
        }

        CardEntity updatedCard = cardRepository.save(card);
        return new CardDTO(updatedCard);
    }

    @Transactional
    public void deleteCard(UUID cardId) {
        CardEntity card = findCardById(cardId);

        UUID columnId = card.getColumn().getId();
        int deletedPos = card.getPosition();

        cardRepository.delete(card);

        List<CardEntity> remainingCards = cardRepository.findByColumn_IdAndArchivedFalseOrderByPositionAsc(columnId);
        for (CardEntity c : remainingCards) {
            if (c.getPosition() > deletedPos) {
                c.setPosition(c.getPosition() - 1);
                cardRepository.save(c);
            }
        }
    }

    @Transactional
    public CardDTO getCardById(UUID cardUUID) {
        CardEntity card = findCardById(cardUUID);
        return new CardDTO(card);
    }

    @Transactional
    public CardMovePayload moveCard(UUID cardId, MoveCardRequest req) {
        CardEntity card = findCardById(cardId);

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
                .findByColumn_IdAndArchivedFalseOrderByPositionAsc(newCol.getId())
                .stream().map(CardDTO::new).toList();

        List<CardDTO> sourceCards = null;
        UUID sourceColumnId = null;
        if (!isSameColumn) {
            sourceColumnId = oldCol.getId();
            sourceCards = cardRepository
                    .findByColumn_IdAndArchivedFalseOrderByPositionAsc(oldCol.getId())
                    .stream().map(CardDTO::new).toList();
        }

        return new CardMovePayload(newCol.getId(), sourceColumnId, targetCards, sourceCards);
    }

    @Transactional
    public CardDTO archiveCard(UUID cardId) {
        CardEntity card = findCardById(cardId);

        UUID columnId = card.getColumn().getId();
        int archivedPos = card.getPosition();

        card.setArchived(true);
        card.setArchivedAt(LocalDateTime.now());

        cardRepository.save(card);

        List<CardEntity> remainingCards = cardRepository.findByColumn_IdAndArchivedFalseOrderByPositionAsc(columnId);

        for (CardEntity c : remainingCards) {
            if (c.getPosition() > archivedPos) {
                c.setPosition(c.getPosition() - 1);
            }
        }

        cardRepository.saveAll(remainingCards);

        return new CardDTO(card);
    }

    @Transactional
    public CardDTO unarchiveCard(UUID cardId) {
        CardEntity card = findCardById(cardId);

        UUID columnId = card.getColumn().getId();

        int nextPosition = cardRepository.findByColumn_IdAndArchivedFalseOrderByPositionAsc(columnId)
                .size();

        card.setArchived(false);
        card.setArchivedAt(null);
        card.setPosition(nextPosition);

        return new CardDTO(cardRepository.save(card));
    }

    // public List<CardDTO> getArchivedCards(UUID spaceId) {
    //     // Lấy tất cả archived cards của space, kể cả card trong column đã bị archive
    //     return cardRepository.findArchivedCardsBySpaceId(spaceId)
    //             .stream()
    //             .map(CardDTO::new)
    //             .toList();
    // }

    public List<CardDTO> getArchivedCards(UUID spaceId) {
        return columnRepository.findBySpaceIdOrderByPositionAsc(spaceId)
                .stream()
                .flatMap(col -> cardRepository
                        .findByColumn_IdAndArchivedTrueOrderByPositionAsc(col.getId())
                        .stream())
                .map(CardDTO::new)
                .toList();
    }
    
    @Transactional
    public void deleteAllArchivedCards(UUID spaceId){
        cardRepository.deleteAllArchivedCards(spaceId);
    }

    private void handleSameColumnMove(ColumnEntity column, CardEntity card, int newPosition) {
        int oldPosition = card.getPosition();
        List<CardEntity> cards = cardRepository.findByColumn_IdAndArchivedFalseOrderByPositionAsc(column.getId())
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
        List<CardEntity> oldCards = cardRepository.findByColumn_IdAndArchivedFalseOrderByPositionAsc(oldCol.getId())
                .stream()
                .filter(c -> !c.getId().equals(card.getId()))
                .collect(Collectors.toList());
        for (CardEntity c : oldCards) {
            if (c.getPosition() > card.getPosition()) {
                c.setPosition(c.getPosition() - 1);
            }
        }
        cardRepository.saveAll(oldCards);

        List<CardEntity> newCards = cardRepository.findByColumn_IdAndArchivedFalseOrderByPositionAsc(newCol.getId());
        for (CardEntity c : newCards) {
            if (c.getPosition() >= newPosition) {
                c.setPosition(c.getPosition() + 1);
            }
        }
        cardRepository.saveAll(newCards);
    }

    public CardDTO completeCard(UUID cardId, boolean completed) {
        CardEntity card = findCardById(cardId);
        card.setCompleted(completed);
        card.setCompletedAt(LocalDateTime.now());

        return new CardDTO(cardRepository.save(card));
    }

    private CardEntity findCardById(UUID cardId) {
        return cardRepository.findById(cardId).orElseThrow(() -> new RuntimeException("Card không tồn tại"));
    }
}