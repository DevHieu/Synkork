package com.synkork.backend.modules.admin.workspace.spaces;

import com.synkork.backend.common.response.ApiResponse;
import com.synkork.backend.modules.admin.workspace.members.dtos.AdminRoomMemberResponse;
import com.synkork.backend.modules.admin.workspace.rooms.AdminRoomService;
import com.synkork.backend.modules.admin.workspace.spaces.dtos.AdminRoomSpaceResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/manage/rooms/{roomId}/spaces")
public class AdminSpaceController {

    @Autowired
    private AdminRoomService adminRoomService;

    @GetMapping()
    public ApiResponse<List<AdminRoomSpaceResponse>> getRoomSpaces(@PathVariable String roomId) {
        return ApiResponse.success("Get room spaces successfully", adminRoomService.getRoomSpaces(roomId));
    }
}
