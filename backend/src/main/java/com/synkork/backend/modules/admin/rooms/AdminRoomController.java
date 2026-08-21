package com.synkork.backend.modules.admin.rooms;

import com.synkork.backend.common.response.ApiResponse;
import com.synkork.backend.common.response.PageMeta;
import com.synkork.backend.modules.admin.rooms.dtos.*;

import com.synkork.backend.modules.roomMember.RoomMemberService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;


import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/manage/rooms")
public class AdminRoomController {

    @Autowired
    private AdminRoomService adminRoomService;
    @Autowired
    private RoomMemberService roomMemberService;

    @GetMapping
    public ApiResponse<List<AdminRoomResponse>> getRooms(@ModelAttribute RoomFilterRequest filter) {
        Page<AdminRoomResponse> list = adminRoomService.getRooms(filter).map(AdminRoomResponse::new);

        return ApiResponse.success("Lấy danh sách room thành công", list.getContent(), PageMeta.from(list));
    }

    @GetMapping("/{roomId}")
    public ApiResponse<AdminRoomDetailResponse> getRoomDetail(@PathVariable String roomId) {
        return ApiResponse.success("Lấy chi tiết room thành công", adminRoomService.getRoomDetail(roomId));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<AdminRoomResponse> createRoom(@RequestBody AdminRoomRequest request) {
        AdminRoomResponse roomEntity = adminRoomService.createRoom(request);
        roomMemberService.addRoomMembers(
                roomEntity.getOwnerId(),
                roomEntity.getId().toString(),
                "OWNER"
        );
        return ApiResponse.success("Tạo room thành công", roomEntity);
    }

    @PutMapping("/{roomId}")
    public ApiResponse<AdminRoomResponse> updateRoom(
            @PathVariable String roomId,
            @Valid @RequestBody AdminRoomRequest request
    ) {
        return ApiResponse.success("Cập nhật room thành công", adminRoomService.updateRoom(roomId, request));
    }

    @GetMapping("/owners/search")
    public ApiResponse<List<AdminUserOptionResponse>> searchOwners(
            @RequestParam(name = "keyword", required = false, defaultValue = "") String keyword
    ) {
        return ApiResponse.success("Tìm kiếm người dùng thành công", adminRoomService.searchUserOptions(keyword));
    }

    @PatchMapping("/{roomId}/status")
    public ApiResponse<AdminRoomResponse> toggleRoomStatus(@PathVariable UUID roomId, @Valid @RequestBody ToggleRoomStatusRequest request){
        return ApiResponse.success("Cập nhật trạng thái room thành công", adminRoomService.toggleRoomStatus(roomId, request));
    }

    @PatchMapping("/{roomId}/warn")
    public ApiResponse<AdminRoomResponse> warnRoom(@PathVariable UUID roomId) {
        return ApiResponse.success("Cảnh báo room thành công", adminRoomService.warnRoom(roomId));
    }

    @GetMapping("/{roomId}/members")
    public ApiResponse<List<AdminRoomMemberResponse>> getMembers(@PathVariable String roomId) {
        return ApiResponse.success("Lấy danh sách member thành công", adminRoomService.getRoomMembers(roomId));
    }

    @GetMapping("/{roomId}/spaces")
    public ApiResponse<List<AdminRoomSpaceResponse>> getSpaces(@PathVariable String roomId) {
        return ApiResponse.success("Lấy danh sách space thành công", adminRoomService.getRoomSpaces(roomId));
    }
}