package com.synkork.backend.modules.room;

import com.synkork.backend.modules.room.dto.CreateRoomDto;
import com.synkork.backend.modules.room.dto.RoomDto;
import com.synkork.backend.modules.room.dto.RoomReviewResponse;
import com.synkork.backend.modules.roomMember.RoomMemberService;
import com.synkork.backend.modules.space.SpaceService;
import com.synkork.backend.modules.space.dto.CreateSpaceDto;
import com.synkork.backend.security.UserPrinciple;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@MultipartConfig
@RestController
@RequestMapping("/rooms")
public class RoomController {

    @Autowired
    RoomService roomService;

    @Autowired
    SpaceService spaceService;

    @Autowired
    RoomMemberService roomMemberService;

    @Transactional
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createRoom(
            @ModelAttribute CreateRoomDto roomData
    ) {
        System.out.println("roomData: " + roomData);
        try {
            RoomEntity roomEntity = roomService.createRoom(roomData)
                    .orElseThrow(() -> new RuntimeException("Room creation failed"));

            roomMemberService.addRoomMembers(
                    roomData.ownerId(),
                    roomEntity.getId().toString(),
                    "OWNER"
            );

            CreateSpaceDto space = new CreateSpaceDto("Chung", "CHAT");

            spaceService.createSpace(space, roomEntity.getId());

            RoomDto roomDto = new RoomDto(
                    roomEntity.getId(),
                    roomEntity.getName(),
                    roomEntity.getDescription(),
                    roomEntity.getAvatarUrl());

            return ResponseEntity.status(HttpStatus.CREATED).body(roomDto);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage()); // 400
        } catch (Exception e) {// 500
            return ResponseEntity.internalServerError().body("Room creation failed: " + e.getMessage());
        }
    }

    @GetMapping("/me")
    public ResponseEntity<List<RoomDto>> findAllByUserId() {
        UserPrinciple user = (UserPrinciple) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<RoomEntity> rooms = roomService.findRoomUserJoined(user.getId());

        List<RoomDto> roomDtos = rooms.stream().map(room -> new RoomDto(
                room.getId(),
                room.getName(),
                room.getDescription(),
                room.getAvatarUrl()
        )).toList();

        return ResponseEntity.ok(roomDtos);
    }

    @GetMapping("/invites/{code}")
    public ResponseEntity<RoomReviewResponse> getRoomByInviteCode(@PathVariable String code) {
        return ResponseEntity.ok(roomService.getRoomByInviteCode(code));
    }

    @PostMapping("/invites/{code}/join")
    public ResponseEntity<RoomDto> joinRoom(@PathVariable String code) {
        UserPrinciple userPrinciple = (UserPrinciple) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        // trả về subscribe cho socket làm trong service
        RoomDto room = roomService.joinRoom(code, userPrinciple.getId());
        
        return ResponseEntity.ok(room);
    }

    @PostMapping("/{roomId}/invites/reset")
    public ResponseEntity<String> resetInviteCode(@PathVariable String roomId) {
        return ResponseEntity.ok(roomService.resetInviteCode(roomId));
    }

    @GetMapping("/{roomId}/invites")  // lấy code hiện tại để hiển thị
    public ResponseEntity<String> getInviteCode(@PathVariable String roomId) {
        RoomEntity room = roomService.findById(UUID.fromString(roomId));
        return ResponseEntity.ok(room.getInviteCode());
    }
}
