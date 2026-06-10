package com.synkork.backend.modules.admin.workspace.members;

import com.synkork.backend.common.response.ApiResponse;
import com.synkork.backend.modules.admin.workspace.members.dtos.AdminRoomMemberResponse;
import com.synkork.backend.modules.admin.workspace.rooms.AdminRoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/manage/rooms/{roomId}/members")
public class AdminMemberController {

    @Autowired
    private AdminRoomService adminRoomService;

    @GetMapping()
    public ApiResponse<List<AdminRoomMemberResponse>> getRoomMembers(@PathVariable String roomId) {
        return ApiResponse.success("Get room members successfully", adminRoomService.getRoomMembers(roomId));
    }
}
