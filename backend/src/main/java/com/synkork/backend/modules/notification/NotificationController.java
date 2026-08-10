package com.synkork.backend.modules.notification;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.synkork.backend.security.UserPrinciple;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<List<NotificationDTO>> getAll() {
        UserPrinciple principal = (UserPrinciple) SecurityContextHolder
                .getContext().getAuthentication().getPrincipal();
        UUID userId = principal.getId();
        return ResponseEntity.ok(notificationService.getNotifications(userId));
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<Void> markRead(@PathVariable UUID id) {
        UserPrinciple principal = (UserPrinciple) SecurityContextHolder
                .getContext().getAuthentication().getPrincipal();
        notificationService.markAsRead(id, principal.getId());
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/read-all")
    public ResponseEntity<Void> markReadAll() {
        UserPrinciple principal = (UserPrinciple) SecurityContextHolder
                .getContext().getAuthentication().getPrincipal();
        notificationService.markAllAsRead(principal.getId());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{notiId}")
    public ResponseEntity<Void> delete(@PathVariable String notiId){
        UUID notiUUID = UUID.fromString(notiId);

        notificationService.deleteNotication(notiUUID);

        return ResponseEntity.noContent().build();
    }
}
