package com.synkork.backend.modules.room;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;

import com.synkork.backend.modules.room.dto.RoomDto;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/rooms")
public class RoomController {

  @Autowired
  RoomService roomService;

  @PostMapping
  public ResponseEntity<RoomDto> createRoom(@NonNull @RequestBody RoomDto entity) {

    RoomEntity roomEntity = roomService.createSpace(entity)
        .orElseThrow(() -> new RuntimeException("Room creation failed"));

    RoomDto roomDto = new RoomDto(
            roomEntity.getId(),
        roomEntity.getName(),
        roomEntity.getRoomAvatar(),
        roomEntity.getOwner() != null ? roomEntity.getOwner().getId() : null);
    return ResponseEntity.ok(roomDto);
  }

  @GetMapping("/{userId}")
  public ResponseEntity<List<RoomDto>> findAllByUserId(@NonNull @PathVariable UUID userId) {
      List<RoomEntity> rooms = roomService.findRoomUserJoined(userId);

      List<RoomDto> roomDtos = rooms.stream().map(room -> new RoomDto(
              room.getId(),
              room.getName(),
              room.getRoomAvatar(),
              room.getOwner().getId() // Jackson sẽ không lỗi vì ta chỉ lấy ID (là kiểu UUID xịn)
      )).toList();

      return ResponseEntity.ok(roomDtos);
  }
}
