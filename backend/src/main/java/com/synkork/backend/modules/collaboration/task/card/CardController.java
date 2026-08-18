package com.synkork.backend.modules.collaboration.task.card;

import com.synkork.backend.security.UserPrinciple;

import java.util.List;
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
import com.synkork.backend.modules.collaboration.task.dto.CardMovePayload;
import com.synkork.backend.modules.collaboration.task.dto.CardRequest;
import com.synkork.backend.modules.collaboration.task.dto.CompleteCardRequest;
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
    public ResponseEntity<CardDTO> createCard(@PathVariable String spaceId, @RequestBody CardRequest req) {
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

        messagingTemplate.convertAndSend("/topic/space/" + spaceId + "/card/delete", cardId);

        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{cardId}/move")
    public ResponseEntity<Void> moveCard(@PathVariable String cardId, @PathVariable String spaceId,
            @RequestBody MoveCardRequest req) {
        UUID cardUUID = UUID.fromString(cardId);
        CardMovePayload payload = cardService.moveCard(cardUUID, req);

        messagingTemplate.convertAndSend("/topic/space/" + spaceId + "/card/move", payload);

        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{cardId}/archive")
    public ResponseEntity<CardDTO> archiveCard(@PathVariable String cardId, @PathVariable String spaceId) {
        UUID cardUUID = UUID.fromString(cardId);
        CardDTO result = cardService.archiveCard(cardUUID);
        messagingTemplate.convertAndSend("/topic/space/" + spaceId + "/card/archive", result);
        return ResponseEntity.ok(result);
    }

    @PatchMapping("/{cardId}/unarchive")
    public ResponseEntity<CardDTO> unarchiveCard(@PathVariable String cardId, @PathVariable String spaceId) {
        UUID cardUUID = UUID.fromString(cardId);
        CardDTO result = cardService.unarchiveCard(cardUUID);
        messagingTemplate.convertAndSend("/topic/space/" + spaceId + "/card/unarchive", result);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/archived")
    public ResponseEntity<List<CardDTO>> getArchivedCards(@PathVariable String spaceId) {
        return ResponseEntity.ok(cardService.getArchivedCards(UUID.fromString(spaceId)));
    }

    @DeleteMapping("/archived/all")
    public ResponseEntity<Void> deleteAllArchivedCards(@PathVariable String spaceId){
        cardService.deleteAllArchivedCards(UUID.fromString(spaceId));

        messagingTemplate.convertAndSend("/topic/space/" + spaceId + "/card/deleteAllArchived", "deleted");

        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{cardId}/complete")
    public ResponseEntity<CardDTO> completeCard(@PathVariable String cardId, @PathVariable String spaceId, @RequestBody CompleteCardRequest req) {
        UUID cardUUID = UUID.fromString(cardId);
        CardDTO result = cardService.completeCard(cardUUID, req);
        messagingTemplate.convertAndSend("/topic/space/" + spaceId + "/card/complete", result);
        return ResponseEntity.ok(result);
    }

}
