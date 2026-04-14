package com.synkork.backend.modules.collaboration.task.column;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.synkork.backend.modules.collaboration.task.board.BoardEntity;
import com.synkork.backend.modules.collaboration.task.board.BoardRepository;
import com.synkork.backend.modules.collaboration.task.dto.ColumnDTO;
import com.synkork.backend.modules.collaboration.task.dto.ColumnRequest;
import com.synkork.backend.modules.collaboration.task.dto.MoveColumnRequest;

import jakarta.transaction.Transactional;

@Service
public class ColumnService {
    @Autowired
    private ColumnRepository columnRepository;

    @Autowired
    private BoardRepository boardRepository;

    @Transactional
    public ColumnDTO createColumn(UUID boardId, ColumnRequest req){
        BoardEntity board = boardRepository.findById(boardId)
                .orElseThrow(() -> new RuntimeException("Board không tồn tại!"));
        
        int nextPosition = columnRepository.findByBoardIdOrderByPositionAsc(boardId).size();

        ColumnEntity col = new ColumnEntity();
        col.setBoard(board);
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
            
        UUID boardId = col.getBoard().getId();
        int deletePos = col.getPosition();

        columnRepository.delete(col);

        List<ColumnEntity> remainingCols = columnRepository.findByBoardIdOrderByPositionAsc(boardId);
        for(ColumnEntity c : remainingCols){
            if(c.getPosition() > deletePos){
                c.setPosition(c.getPosition() - 1);
                columnRepository.save(c);
            }
        }
    }

    @Transactional
    public void moveColumn(UUID columnId, MoveColumnRequest req){
        ColumnEntity col = columnRepository.findById(columnId)
                .orElseThrow(() -> new RuntimeException("Cột không tồn tại!"));
    
        col.setPosition(req.getNewPosition());
        
        columnRepository.save(col);
    }

    public ColumnDTO getColumnById(UUID columnId) {
        ColumnEntity col = columnRepository.findById(columnId)
                .orElseThrow(() -> new RuntimeException("Cột không tồn tại!"));

        return new ColumnDTO(col);
    }

    public UUID getBoardIdByColumnId(UUID columnId) {
        return columnRepository.findById(columnId)
                .map(column -> column.getBoard().getId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy cột"));
    }
}
