package com.synkork.backend.modules.collaboration.task.column;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.synkork.backend.modules.collaboration.task.dto.ColumnDTO;
import com.synkork.backend.modules.collaboration.task.dto.ColumnRequest;
import com.synkork.backend.modules.collaboration.task.dto.MoveColumnRequest;
import com.synkork.backend.modules.space.SpaceEntity;
import com.synkork.backend.modules.space.SpaceRepository;

import jakarta.transaction.Transactional;

@Service
public class ColumnService {
    @Autowired
    private ColumnRepository columnRepository;

    @Autowired
    private SpaceRepository spaceRepository;

    @Transactional
    public ColumnDTO createColumn(UUID spaceId, ColumnRequest req){
        SpaceEntity space = spaceRepository.findById(spaceId)
                .orElseThrow(() -> new RuntimeException("space không tồn tại!"));
        
        int nextPosition = columnRepository.findBySpaceIdOrderByPositionAsc(spaceId).size();

        ColumnEntity col = new ColumnEntity();
        col.setSpace(space);
        col.setName(req.getName());
        col.setPosition(nextPosition);

        ColumnEntity saveCol = columnRepository.save(col);

        return new ColumnDTO(saveCol);
    }

    @Transactional
    public ColumnDTO updateColumn(UUID columnId, ColumnRequest req){
        ColumnEntity col = columnRepository.findById(columnId)
                .orElseThrow(() -> new RuntimeException("Cột không tồn tại!"));

        col.setName(req.getName());

        ColumnEntity updatedCol = columnRepository.save(col);
        return new ColumnDTO(updatedCol);
    } 

    @Transactional 
    public void deleteColumn(UUID columnId){
        ColumnEntity col = columnRepository.findById(columnId)
                .orElseThrow(() -> new RuntimeException("Cột không tồn tại!"));
            
        UUID spaceId = col.getSpace().getId();
        int deletePos = col.getPosition();

        columnRepository.delete(col);

        List<ColumnEntity> remainingCols = columnRepository.findBySpaceIdOrderByPositionAsc(spaceId);
        for(ColumnEntity c : remainingCols){
            if(c.getPosition() > deletePos){
                c.setPosition(c.getPosition() - 1);
                columnRepository.save(c);
            }
        }
    }

    @Transactional
    public ColumnDTO moveColumn(UUID columnId, MoveColumnRequest req) {

        ColumnEntity movingCol = columnRepository.findById(columnId)
                .orElseThrow(() -> new RuntimeException("Cột không tồn tại!"));

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
        ColumnEntity col = columnRepository.findById(columnId)
                .orElseThrow(() -> new RuntimeException("Cột không tồn tại!"));

        return new ColumnDTO(col);
    }
    @Transactional
    public List<ColumnDTO> getAll(UUID spaceId){
        List<ColumnEntity> columns = columnRepository.findBySpaceIdOrderByPositionAsc(spaceId);

        return columns.stream().map(ColumnDTO::new).collect(Collectors.toList());
    }

    @Transactional()
    public List<ColumnDTO> getAllWithoutCards(UUID spaceId) {
        List<ColumnEntity> columns = columnRepository.findColumnsOnlyBySpaceId(spaceId);
        return columns.stream().map(ColumnDTO::new).collect(Collectors.toList());
    }
}
