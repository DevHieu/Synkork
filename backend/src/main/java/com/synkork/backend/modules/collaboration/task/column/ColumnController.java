package com.synkork.backend.modules.collaboration.task.column;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<ColumnDTO> getColumnById(@PathVariable String columnId) {
        UUID columnUUID = UUID.fromString(columnId);
        return ResponseEntity.ok(columnService.getColumnById(columnUUID));
    }

    @GetMapping
    public ResponseEntity<List<ColumnDTO>> getAlls(@PathVariable String spaceId,
            @RequestParam(defaultValue = "true") boolean includeCards) {
        UUID spaceUUID = UUID.fromString(spaceId);
        if (includeCards) {
            return ResponseEntity.ok(columnService.getAll(spaceUUID));
        } else {
            return ResponseEntity.ok(columnService.getAllWithoutCards(spaceUUID));
        }
    }

    @PostMapping
    public ResponseEntity<ColumnDTO> createColumn(@PathVariable String spaceId, @RequestBody ColumnRequest req) {
        UUID spaceUUID = UUID.fromString(spaceId);

        ColumnDTO createdColumn = columnService.createColumn(spaceUUID, req);

        messagingTemplate.convertAndSend("/topic/space/" + spaceId + "/column/create", createdColumn);
        return ResponseEntity.ok(createdColumn);
    }

    @PutMapping("/{columnId}")
    public ResponseEntity<ColumnDTO> updateColumn(@PathVariable String columnId, @RequestBody ColumnRequest req) {
        UUID columnUUID = UUID.fromString(columnId);

        ColumnDTO updatedColumn = columnService.updateColumn(columnUUID, req);

        messagingTemplate.convertAndSend("/topic/space/" + updatedColumn.getSpaceId() + "/column/update",
                updatedColumn);

        return ResponseEntity.ok(updatedColumn);
    }

    @DeleteMapping("/{columnId}")
    public ResponseEntity<Void> deleteColumn(@PathVariable String spaceId, @PathVariable String columnId) {
        UUID columnUUID = UUID.fromString(columnId);

        columnService.deleteColumn(columnUUID);

        messagingTemplate.convertAndSend("/topic/space/" + spaceId + "/column/delete", columnId);

        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{columnId}/move")
    public ResponseEntity<Void> moveColumn(@PathVariable String columnId, @PathVariable String spaceId,
            @RequestBody MoveColumnRequest req) {
        UUID columnUUID = UUID.fromString(columnId);

        ColumnDTO movedColumn = columnService.moveColumn(columnUUID, req);

        messagingTemplate.convertAndSend("/topic/space/" + spaceId + "/column/move", movedColumn);

        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{columnId}/archive")
    public ResponseEntity<ColumnDTO> archiveColumn(@PathVariable String columnId, @PathVariable String spaceId) {
        UUID columnUUID = UUID.fromString(columnId);
        ColumnDTO result = columnService.archiveColumn(columnUUID);
        messagingTemplate.convertAndSend("/topic/space/" + spaceId + "/column/archive", result);
        return ResponseEntity.ok(result);
    }

    @PatchMapping("/{columnId}/unarchive")
    public ResponseEntity<ColumnDTO> unarchiveColumn(@PathVariable String columnId, @PathVariable String spaceId) {
        UUID columnUUID = UUID.fromString(columnId);
        ColumnDTO result = columnService.unarchiveColumn(columnUUID);
        messagingTemplate.convertAndSend("/topic/space/" + spaceId + "/column/unarchive", result);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/archived")
    public ResponseEntity<List<ColumnDTO>> getArchivedColumns(@PathVariable String spaceId) {
        return ResponseEntity.ok(columnService.getArchivedColumns(UUID.fromString(spaceId)));
    }

    @DeleteMapping("/archived/all")
    public ResponseEntity<Void> deleteAllArchivedColumns(@PathVariable String spaceId){
        columnService.deleteAllArchivedColumns(UUID.fromString(spaceId));
        messagingTemplate.convertAndSend("/topic/space/" + spaceId + "/column/deleteAllArchived", "deleted");
        return ResponseEntity.noContent().build();
    }
}
