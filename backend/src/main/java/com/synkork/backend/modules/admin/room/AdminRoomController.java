package com.synkork.backend.modules.admin.room;

import com.synkork.backend.modules.admin.room.dto.AdminRoomDetailResponse;
import com.synkork.backend.modules.admin.room.dto.AdminRoomRequest;
import com.synkork.backend.modules.admin.room.dto.AdminRoomResponse;
import com.synkork.backend.modules.admin.room.dto.RoomFilterRequest;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/manage/rooms")
public class AdminRoomController {

    private final AdminRoomService adminRoomService;

    public AdminRoomController(AdminRoomService adminRoomService) {
        this.adminRoomService = adminRoomService;
    }

    @GetMapping
    public ResponseEntity<Page<AdminRoomResponse>> getRooms(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        RoomFilterRequest filter = new RoomFilterRequest();
        filter.setSearch(search);
        filter.setPage(page);
        filter.setSize(size);

        return ResponseEntity.ok(adminRoomService.getRooms(filter));
    }

    @GetMapping("/{roomId}")
    public ResponseEntity<AdminRoomDetailResponse> getRoomDetail(@PathVariable String roomId) {
        return ResponseEntity.ok(adminRoomService.getRoomDetail(roomId));
    }

    @PostMapping
    public ResponseEntity<AdminRoomResponse> createRoom(@RequestBody AdminRoomRequest request) {
        return ResponseEntity.ok(adminRoomService.createRoom(request));
    }

    @PutMapping("/{roomId}")
    public ResponseEntity<AdminRoomResponse> updateRoom(
            @PathVariable String roomId,
            @RequestBody AdminRoomRequest request
    ) {
        return ResponseEntity.ok(adminRoomService.updateRoom(roomId, request));
    }

    @DeleteMapping("/{roomId}")
    public ResponseEntity<Void> deleteRoom(@PathVariable String roomId) {
        adminRoomService.deleteRoom(roomId);
        return ResponseEntity.noContent().build();
    }
}