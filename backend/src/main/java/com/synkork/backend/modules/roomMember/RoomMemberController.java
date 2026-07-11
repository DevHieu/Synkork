package com.synkork.backend.modules.roomMember;

import com.synkork.backend.common.utils.AuthUtils;
import com.synkork.backend.modules.roomMember.dto.ChatDisableRequest;
import com.synkork.backend.modules.roomMember.dto.ChangeAuthorityDTO;
import com.synkork.backend.modules.roomMember.dto.MuteRequest;
import com.synkork.backend.modules.roomMember.dto.RoomMemberDto;
import com.synkork.backend.security.UserPrinciple;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/rooms/{roomId}/members")
public class RoomMemberController {

    @Autowired
    RoomMemberService roomMemberService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @GetMapping
    public ResponseEntity<List<RoomMemberDto>> getRoomMembers(@PathVariable String roomId) {
        return ResponseEntity.ok(roomMemberService.getRoomMembers(roomId));
    }

    @PostMapping("/{userId}")
    public ResponseEntity<RoomMemberDto> addRoomMembers(@PathVariable String roomId, @PathVariable String userId, @RequestParam String role) {
        RoomMemberEntity entity = roomMemberService.addRoomMembers(userId, roomId, role);
        RoomMemberDto dto = new RoomMemberDto(entity);

        messagingTemplate.convertAndSend("/topic/room/" + roomId + "/members/joined", dto);
        return ResponseEntity.ok(dto);
    }

    @PutMapping("/change-authority")
    public ResponseEntity<RoomMemberDto> changeAuthority(@PathVariable String roomId, @RequestBody ChangeAuthorityDTO dto) {
        UUID requesterId = AuthUtils.getCurrentUserId();
        UUID roomUUID = UUID.fromString(roomId);

        RoomMemberEntity member = roomMemberService.changerAuthority(dto, roomUUID, requesterId);
        RoomMemberDto resp = new RoomMemberDto(member);

        messagingTemplate.convertAndSend(
                "/topic/room/" + roomId + "/members/changeAuthority", resp
        );

        return ResponseEntity.ok(resp);
    }

    @DeleteMapping("/{memberId}")
    public ResponseEntity<Void> deleteRoomMembers(@PathVariable String roomId, @PathVariable String memberId) {
        UUID memberUUID = UUID.fromString(memberId);
        UUID roomUUID = UUID.fromString(roomId);
        UUID requesterId = AuthUtils.getCurrentUserId();

        String targetEmail = roomMemberService.kickMember(memberUUID, roomUUID, requesterId);

        messagingTemplate.convertAndSend("/topic/room/" + roomId + "/members/kicked", memberId);
        // này là subscribe để đứa bị đuổi nó biết
        messagingTemplate.convertAndSendToUser(targetEmail, "/queue/kick", roomId);

        return  ResponseEntity.ok().build();
    }

    @PatchMapping("/{memberId}/mute")
    public ResponseEntity<Void> muteRoomMembers(@PathVariable String roomId, @PathVariable String memberId, @RequestBody MuteRequest muteRequest) {
        UUID roomUUID =  UUID.fromString(roomId);
        UUID memberUUID = UUID.fromString(memberId);
        UUID requesterId = AuthUtils.getCurrentUserId();

        RoomMemberEntity member = roomMemberService.toggleMuteMembers(roomUUID, memberUUID, requesterId, muteRequest);
        RoomMemberDto dto = new RoomMemberDto(member);

        messagingTemplate.convertAndSend(
                "/topic/room/" + roomId + "/members/updated", dto
        );

        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{memberId}/chat-disable")
    public ResponseEntity<RoomMemberDto> changeChatDisable(@PathVariable String roomId, @PathVariable String memberId, @RequestBody ChatDisableRequest request) {
        UUID roomUUID = UUID.fromString(roomId);
        UUID memberUUID = UUID.fromString(memberId);
        UUID requesterId = AuthUtils.getCurrentUserId();

        RoomMemberEntity member = roomMemberService.changeChatDisable(roomUUID, memberUUID, requesterId, request);
        RoomMemberDto dto = new RoomMemberDto(member);

        messagingTemplate.convertAndSend(
                "/topic/room/" + roomId + "/members/updated", dto
        );

        return ResponseEntity.ok(dto);
    }

    @DeleteMapping("/leave")
    public ResponseEntity<?> leaveRoom(@PathVariable String roomId) {
        UUID roomUUID = UUID.fromString(roomId);
        UUID requesterId = AuthUtils.getCurrentUserId();

        roomMemberService.leaveRoom(roomUUID, requesterId);

        return ResponseEntity.ok().build();
    }
}
