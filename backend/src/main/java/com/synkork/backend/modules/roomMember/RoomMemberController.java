package com.synkork.backend.modules.roomMember;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/rooms/{roomId}/members")
public class RoomMemberController {

    @Autowired
    RoomMemberService roomMemberService;

    @GetMapping("")
    public ResponseEntity<?> getRoomMembers(@PathVariable String roomId) {
        return ResponseEntity.ok(roomMemberService.getRoomMembers(roomId));
    }

    @PostMapping("/{userId}")
    public ResponseEntity<RoomMemberEntity> addRoomMembers(@PathVariable String roomId, @PathVariable String userId, @RequestParam String role) {
        return ResponseEntity.ok(roomMemberService.addRoomMembers(userId, roomId, role));
    }
}
