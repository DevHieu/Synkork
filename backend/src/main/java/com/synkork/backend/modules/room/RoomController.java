package com.synkork.backend.modules.room;

import com.synkork.backend.modules.room.dto.CreateRoomDto;
import com.synkork.backend.modules.room.dto.RoomDto;
import com.synkork.backend.modules.space.SpaceEntity;
import com.synkork.backend.modules.space.SpaceService;
import com.synkork.backend.modules.space.SpaceTypeEnum;
import com.synkork.backend.modules.space.dto.CreateSpaceDto;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
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

    @Transactional
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createRoom(
            @ModelAttribute CreateRoomDto roomData
    ) {
        System.out.println("roomData: " + roomData);
        try {
            RoomEntity roomEntity = roomService.createRoom(roomData)
                    .orElseThrow(() -> new RuntimeException("Room creation failed"));

            roomService.addRoomMembers(
                    roomData.ownerId(),
                    roomEntity.getId().toString(),
                    "OWNER"
            );

            CreateSpaceDto space = new CreateSpaceDto("Chung", "CHAT");

            spaceService.createSpace(space, roomEntity.getId());

            RoomDto roomDto = new RoomDto(
                    roomEntity.getId(),
                    roomEntity.getName(),
                    roomEntity.getAvatarUrl());

            return ResponseEntity.status(HttpStatus.CREATED).body(roomDto);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage()); // 400
        } catch (Exception e) {// 500
            return ResponseEntity.internalServerError().body("Room creation failed: " + e.getMessage());
        }
    }

    @PostMapping("/{roomId}/members/{userId}")
    public ResponseEntity<RoomMemberEntity> addRoomMembers(@PathVariable String roomId, @PathVariable String userId, @RequestParam String role) {
        return ResponseEntity.ok(roomService.addRoomMembers(userId, roomId, role));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<List<RoomDto>> findAllByUserId(@NonNull @PathVariable UUID userId) {
        List<RoomEntity> rooms = roomService.findRoomUserJoined(userId);

        List<RoomDto> roomDtos = rooms.stream().map(room -> new RoomDto(
                room.getId(),
                room.getName(),
                room.getAvatarUrl()
        )).toList();

        return ResponseEntity.ok(roomDtos);
    }
}
