package com.synkork.backend.modules.collaboration.task.card;

import com.synkork.backend.security.UserPrinciple;

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
@RequestMapping("/space/{spaceId}/card")
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
    public ResponseEntity<CardDTO> createCard(@PathVariable String spaceId, @RequestBody CardRequest req){
        UserPrinciple principal = (UserPrinciple) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String creatorEmail = principal.getUsername();
        UUID spaceUUID = UUID.fromString(spaceId);

        CardDTO createdCard = cardService.createCard(spaceUUID, creatorEmail, req);
        
        messagingTemplate.convertAndSend("/topic/space/" + spaceId + "/card/create", createdCard);
        
        return ResponseEntity.ok(createdCard);
    }

    @PutMapping("/{cardId}")
    public ResponseEntity<CardDTO> updateCard(@PathVariable String cardId, @RequestBody CardRequest req) {
        UUID cardUUID = UUID.fromString(cardId);
        CardDTO updatedCard = cardService.updateCard(cardUUID, req);
        
        messagingTemplate.convertAndSend("/topic/space/" + updatedCard.getSpaceId() + "/card/update", updatedCard);
        
        return ResponseEntity.ok(updatedCard);
    }

    @DeleteMapping("/{cardId}")
    public ResponseEntity<Void> deleteCard(@PathVariable String cardId, @PathVariable String spaceId) {
        UUID cardUUID = UUID.fromString(cardId);
        cardService.deleteCard(cardUUID);
        
        messagingTemplate.convertAndSend("/topic/space/" + spaceId + "/card/delete",  cardId);
        
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{cardId}/move")
    public ResponseEntity<Void> moveCard(@PathVariable String cardId, @PathVariable String spaceId, @RequestBody MoveCardRequest req){
        UUID cardUUID = UUID.fromString(cardId);

        CardDTO movedCard = cardService.moveCard(cardUUID, req); 

        messagingTemplate.convertAndSend("/topic/space/" + spaceId + "/card/move", movedCard);

        return ResponseEntity.noContent().build();
    }
}
