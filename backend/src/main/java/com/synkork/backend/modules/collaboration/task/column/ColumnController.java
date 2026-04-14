package com.synkork.backend.modules.collaboration.task.column;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.synkork.backend.modules.collaboration.task.dto.ColumnDTO;
import com.synkork.backend.modules.collaboration.task.dto.ColumnRequest;
import com.synkork.backend.modules.collaboration.task.dto.MoveColumnRequest;

@RestController
@RequestMapping("/board/{boardId}/column")
public class ColumnController {
    @Autowired
    private ColumnService columnService;
    
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @GetMapping("/{columnId}")
    public ResponseEntity<ColumnDTO> getColumnById(@PathVariable String columnId){
        UUID columnUUID = UUID.fromString(columnId);
        return ResponseEntity.ok(columnService.getColumnById(columnUUID));
    }

    @PostMapping
    public ResponseEntity<ColumnDTO> createColumn(@PathVariable String boardId, @RequestBody ColumnRequest req){
        UUID boardUUID = UUID.fromString(boardId);

        ColumnDTO createdColumn = columnService.createColumn(boardUUID, req);

        messagingTemplate.convertAndSend("/topic/board" + boardId + "/column", createdColumn);
        return ResponseEntity.ok(createdColumn);
    }

    @PutMapping("/{columnId}")
    public ResponseEntity<ColumnDTO> updateColumn(@PathVariable String columnId, @RequestBody ColumnRequest req){
        UUID columnUUID = UUID.fromString(columnId);

        ColumnDTO updatedColumn = columnService.updateColumn(columnUUID, req);

        messagingTemplate.convertAndSend("/topic/board" + updatedColumn.getBoardId() + "/column", updatedColumn);

        return ResponseEntity.ok(updatedColumn);
    }

    @DeleteMapping("/{columnId}")
    public ResponseEntity<Void> deleteColumn(@PathVariable String columnId){
        UUID columnUUID = UUID.fromString(columnId);
        UUID boardId = columnService.getBoardIdByColumnId(columnUUID);

        columnService.deleteColumn(columnUUID);

        messagingTemplate.convertAndSend("/topic/board" + boardId + "/column" + columnId);
        
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{columnId}/move")
    public ResponseEntity<Void> moveColumn(@PathVariable String columnId, @RequestBody MoveColumnRequest req){
        UUID columnUUID = UUID.fromString(columnId);

        columnService.moveColumn(columnUUID, req);
        
        return ResponseEntity.noContent().build();
    }

}
