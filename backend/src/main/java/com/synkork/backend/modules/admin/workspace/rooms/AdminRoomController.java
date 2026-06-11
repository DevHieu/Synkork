package com.synkork.backend.modules.admin.workspace.rooms;

import com.synkork.backend.common.response.ApiResponse;
import com.synkork.backend.common.response.PageMeta;
import com.synkork.backend.modules.admin.workspace.rooms.dtos.AdminRoomDetailResponse;
import com.synkork.backend.modules.admin.workspace.rooms.dtos.AdminRoomRequest;
import com.synkork.backend.modules.admin.workspace.rooms.dtos.AdminRoomResponse;
import com.synkork.backend.modules.admin.workspace.rooms.dtos.RoomFilterRequest;
import com.synkork.backend.modules.admin.workspace.spaces.dtos.AdminRoomSpaceResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/manage/rooms")
public class AdminRoomController {

    @Autowired
    private AdminRoomService adminRoomService;

    @GetMapping
    public ApiResponse<List<AdminRoomResponse>> getRooms(@ModelAttribute RoomFilterRequest filter) {
        Page<AdminRoomResponse> list = adminRoomService.getRooms(filter).map(AdminRoomResponse::new);

        return ApiResponse.success("Get room list successfully", list.getContent(), PageMeta.from(list));
    }

    @GetMapping("/{roomId}")
    public ApiResponse<AdminRoomDetailResponse> getRoomDetail(@PathVariable String roomId) {
        return ApiResponse.success("Get room detail successfully", adminRoomService.getRoomDetail(roomId));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<AdminRoomResponse> createRoom(@RequestBody AdminRoomRequest request) {
        return ApiResponse.success("Create room successfully", adminRoomService.createRoom(request));
    }

    @PutMapping("/{roomId}")
    public ApiResponse<AdminRoomResponse> updateRoom(
            @PathVariable String roomId,
            @RequestBody AdminRoomRequest request
    ) {
        return ApiResponse.success("Update room successfully", adminRoomService.updateRoom(roomId, request));
    }

    @DeleteMapping("/{roomId}")
    public ApiResponse<Void> deleteRoom(@PathVariable String roomId) {
        adminRoomService.deleteRoom(roomId);
        return ApiResponse.success("Delete room successfully", null);
    }
}