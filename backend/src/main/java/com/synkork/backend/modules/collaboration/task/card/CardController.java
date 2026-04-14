package com.synkork.backend.modules.collaboration.task.card;

import java.nio.file.attribute.UserPrincipal;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.synkork.backend.modules.collaboration.task.dto.CardDTO;
import com.synkork.backend.modules.collaboration.task.dto.CardRequest;
import com.synkork.backend.modules.collaboration.task.dto.MoveCardRequest;

@RestController
@RequestMapping("/column/{columnId}/cards")
public class CardController {
    
    @Autowired
    private CardService cardService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @GetMapping("/{cardId}")
    public ResponseEntity<CardDTO> getCardById(@PathVariable String cardId) {

        UUID cardUUID = UUID.fromString(cardId);
        return ResponseEntity.ok(cardService.getCardById(cardUUID));
    }

    @PostMapping
    public ResponseEntity<CardDTO> createCard(@PathVariable String columnId, @RequestBody CardRequest req){
        UserPrincipal principal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String creatorEmail = principal.getName();
        UUID columnUUID = UUID.fromString(columnId);

        CardDTO createdCard = cardService.createCard(columnUUID, creatorEmail, req);
        
        // Sau khi tạo xong card, gửi thông báo qua WebSocket
        messagingTemplate.convertAndSend("/topic/columns/" + columnId + "/cards", createdCard);
        
        return ResponseEntity.ok(createdCard);
    }

    @PutMapping("/{cardId}")
    public ResponseEntity<CardDTO> updateCard(@PathVariable String cardId, @RequestBody CardRequest req) {
        UUID cardUUID = UUID.fromString(cardId);
        CardDTO updatedCard = cardService.updateCard(cardUUID, req);
        
        // Sau khi cập nhật xong card, gửi thông báo qua WebSocket
        messagingTemplate.convertAndSend("/topic/columns/" + updatedCard.getColumnId() + "/cards", updatedCard);
        
        return ResponseEntity.ok(updatedCard);
    }

    @DeleteMapping("/{cardId}")
    public ResponseEntity<Void> deleteCard(@PathVariable String cardId, @PathVariable String columnId) {
        UUID cardUUID = UUID.fromString(cardId);
        cardService.deleteCard(cardUUID);
        
        // Sau khi xóa xong card, gửi thông báo qua WebSocket
        messagingTemplate.convertAndSend("/topic/columns/" + columnId + "/cards", "deleted:" + cardId);
        
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{cardId}/move")
    public ResponseEntity<Void> moveCard(@PathVariable String cardId, @PathVariable String columnId, @RequestBody MoveCardRequest req){
        UUID cardUUID = UUID.fromString(cardId);

        cardService.moveCard(cardUUID, req);

        messagingTemplate.convertAndSend("/topic/columns/" + columnId + "/cards/move");

        return ResponseEntity.noContent().build();

    }
}
