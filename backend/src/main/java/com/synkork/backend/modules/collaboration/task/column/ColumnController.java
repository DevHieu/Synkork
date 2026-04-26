package com.synkork.backend.modules.collaboration.task.column;

import java.util.List;
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
@RequestMapping("/space/{spaceId}/column")
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

    @GetMapping
    public ResponseEntity<List<ColumnDTO>> getAlls(@PathVariable String spaceId){
        UUID spaceUUID = UUID.fromString(spaceId);
        return ResponseEntity.ok(columnService.getAll(spaceUUID));
    }

    @PostMapping
    public ResponseEntity<ColumnDTO> createColumn(@PathVariable String spaceId, @RequestBody ColumnRequest req){
        UUID spaceUUID = UUID.fromString(spaceId);

        ColumnDTO createdColumn = columnService.createColumn(spaceUUID, req);

        messagingTemplate.convertAndSend("/topic/space/" + spaceId + "/column/create", createdColumn);
        return ResponseEntity.ok(createdColumn);
    }

    @PutMapping("/{columnId}")
    public ResponseEntity<ColumnDTO> updateColumn(@PathVariable String columnId, @RequestBody ColumnRequest req){
        UUID columnUUID = UUID.fromString(columnId);

        ColumnDTO updatedColumn = columnService.updateColumn(columnUUID, req);

        messagingTemplate.convertAndSend("/topic/space/" + updatedColumn.getSpaceId() + "/column/update", updatedColumn);

        return ResponseEntity.ok(updatedColumn);
    }

    @DeleteMapping("/{columnId}")
    public ResponseEntity<Void> deleteColumn(@PathVariable String spaceId, @PathVariable String columnId){
        UUID columnUUID = UUID.fromString(columnId);

        columnService.deleteColumn(columnUUID);

        messagingTemplate.convertAndSend("/topic/space/" + spaceId + "/column/delete", columnId);
        
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{columnId}/move")
    public ResponseEntity<Void> moveColumn(@PathVariable String columnId, @PathVariable String spaceId, @RequestBody MoveColumnRequest req) {
        UUID columnUUID = UUID.fromString(columnId);

        ColumnDTO movedColumn = columnService.moveColumn(columnUUID, req);

        messagingTemplate.convertAndSend("/topic/space/" + spaceId + "/column/move", movedColumn);

        return ResponseEntity.noContent().build();
    }

}
