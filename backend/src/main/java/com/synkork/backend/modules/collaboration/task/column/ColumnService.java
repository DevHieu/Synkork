package com.synkork.backend.modules.collaboration.task.column;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;

import com.synkork.backend.modules.collaboration.task.dto.ColumnDTO;
import com.synkork.backend.modules.collaboration.task.dto.ColumnRequest;
import com.synkork.backend.modules.collaboration.task.dto.MoveColumnRequest;
import com.synkork.backend.modules.collaboration.task.card.CardEntity;
import com.synkork.backend.modules.collaboration.task.card.CardRepository;
import com.synkork.backend.modules.space.SpaceEntity;
import com.synkork.backend.modules.space.SpaceRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

@Service
public class ColumnService {
    @Autowired
    private ColumnRepository columnRepository;

    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private SpaceRepository spaceRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public ColumnDTO createColumn(UUID spaceId, ColumnRequest req) {
        SpaceEntity space = spaceRepository.findById(spaceId)
                .orElseThrow(() -> new RuntimeException("space không tồn tại!"));

        int nextPosition = columnRepository.findBySpaceIdAndArchivedFalseOrderByPositionAsc(spaceId).size();

        ColumnEntity col = new ColumnEntity();
        col.setSpace(space);
        col.setName(req.name());
        col.setPosition(nextPosition);

        ColumnEntity saveCol = columnRepository.save(col);

        return new ColumnDTO(saveCol);
    }

    @Transactional
    public ColumnDTO updateColumn(UUID columnId, ColumnRequest req) {
        ColumnEntity col = findColumnById(columnId);

        
                if (!col.getVersion().equals(req.version())) {
            throw new ObjectOptimisticLockingFailureException(ColumnEntity.class, col.getId());
        }
        col.setName(req.name());
        
        ColumnEntity updatedCol = columnRepository.save(col);
        return new ColumnDTO(updatedCol);
    }

    @Transactional
    public void deleteColumn(UUID columnId) {
        ColumnEntity col = findColumnById(columnId);

        UUID spaceId = col.getSpace().getId();
        int deletePos = col.getPosition();

        columnRepository.delete(col);

        List<ColumnEntity> remainingCols = columnRepository.findBySpaceIdOrderByPositionAsc(spaceId);
        for (ColumnEntity c : remainingCols) {
            if (c.getPosition() > deletePos) {
                c.setPosition(c.getPosition() - 1);
                columnRepository.save(c);
            }
        }
    }

    @Transactional
    public ColumnDTO moveColumn(UUID columnId, MoveColumnRequest req) {
        ColumnEntity movingCol = findColumnById(columnId);

        UUID spaceId = movingCol.getSpace().getId();

        List<ColumnEntity> columns = columnRepository.findBySpaceIdOrderByPositionAsc(spaceId);

        columns.removeIf(c -> c.getId().equals(columnId));

        columns.add(req.getNewPosition(), movingCol);

        for (int i = 0; i < columns.size(); i++) {
            columns.get(i).setPosition(i);
        }

        columnRepository.saveAll(columns);

        return new ColumnDTO(movingCol);
    }

    @Transactional
    public ColumnDTO getColumnById(UUID columnId) {
        ColumnEntity col = findColumnById(columnId);

        return new ColumnDTO(col);
    }

    @Transactional
    public List<ColumnDTO> getAll(UUID spaceId) {
        List<ColumnEntity> columns = columnRepository.findBySpaceIdAndArchivedFalseOrderByPositionAsc(spaceId);

        return columns.stream().map(ColumnDTO::new).collect(Collectors.toList());
    }

    @Transactional()
    public List<ColumnDTO> getAllWithoutCards(UUID spaceId) {
        List<ColumnEntity> columns = columnRepository.findColumnsOnlyBySpaceId(spaceId);
        return columns.stream().map(ColumnDTO::new).collect(Collectors.toList());
    }

    @Transactional
    public ColumnDTO archiveColumn(UUID columnId) {
        ColumnEntity col = findColumnById(columnId);

        UUID spaceId = col.getSpace().getId();
        int archivedPos = col.getPosition();

        LocalDateTime archiveTimestamp = LocalDateTime.now();

        col.setArchived(true);
        col.setArchivedAt(archiveTimestamp);

        List<com.synkork.backend.modules.collaboration.task.card.CardEntity> cards =
                cardRepository.findByColumn_IdAndArchivedFalseOrderByPositionAsc(col.getId());

        cards.forEach(card -> {
            card.setArchived(true);
            card.setArchivedAt(archiveTimestamp);
        });

        cardRepository.saveAll(cards);
        columnRepository.save(col);

        List<ColumnEntity> remainingCols =
                columnRepository.findBySpaceIdAndArchivedFalseOrderByPositionAsc(spaceId);

        for (ColumnEntity c : remainingCols) {
            if (c.getPosition() > archivedPos) {
                c.setPosition(c.getPosition() - 1);
            }
        }

        columnRepository.saveAll(remainingCols);

        return new ColumnDTO(col);
    }

    @Transactional
    public ColumnDTO unarchiveColumn(UUID columnId) {
        ColumnEntity col = findColumnById(columnId);

        UUID spaceId = col.getSpace().getId();
         
        LocalDateTime colArchivedAt = col.getArchivedAt();
    
        int nextPosition = columnRepository.findBySpaceIdAndArchivedFalseOrderByPositionAsc(spaceId).size();

        col.setArchived(false);
        col.setArchivedAt(null);
        col.setPosition(nextPosition);
        columnRepository.save(col);

        List<com.synkork.backend.modules.collaboration.task.card.CardEntity> archivedCards =
                cardRepository.findByColumn_IdAndArchivedTrueOrderByPositionAsc(col.getId());

        List<CardEntity> cardsRestore = archivedCards.stream()
                                                     .filter(c -> colArchivedAt != null && colArchivedAt.equals(c.getArchivedAt()))
                                                     .toList();

        cardsRestore.forEach(card -> {
            card.setArchived(false);
            card.setArchivedAt(null);
        });

        cardRepository.saveAll(cardsRestore);

        entityManager.flush();
        entityManager.refresh(col);

        return new ColumnDTO(col);
    }

    public List<ColumnDTO> getArchivedColumns(UUID spaceId) {
        return columnRepository.findBySpaceIdAndArchivedTrueOrderByPositionAsc(spaceId)
                .stream().map(ColumnDTO::new).toList();
    }

    @Transactional
    public void deleteAllArchivedColumns(UUID spaceId) {
        List<ColumnEntity> archivedCols = columnRepository.findBySpaceIdAndArchivedTrueOrderByPositionAsc(spaceId);
        List<UUID> columnIds = archivedCols.stream()    
                                           .map(ColumnEntity::getId)
                                           .toList();

        if(!columnIds.isEmpty()) cardRepository.deleteByColumn_IdIn(columnIds);
        
        columnRepository.deleteAllArchivedColumns(spaceId);
    }

    private ColumnEntity findColumnById(UUID columnId) {
        return columnRepository.findById(columnId).orElseThrow(() -> new RuntimeException("Cột không tồn tại"));
    }
}