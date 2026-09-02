package com.synkork.backend.common.utils;

import com.synkork.backend.modules.roomMember.RoomMemberEntity;
import com.synkork.backend.modules.roomMember.RoomMemberRepository;
import com.synkork.backend.modules.roomMember.enums.RoomMemberRoleEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.UUID;

@Service
public class PermissionService {

    private static RoomMemberRepository roomMemberRepository;

    // Ghi kiểu này để có thể dùng static được
    @Autowired
    public void setRoomMemberRepository(RoomMemberRepository roomMemberRepository) {
        PermissionService.roomMemberRepository = roomMemberRepository;
    }

    // Overload 1: Đã có entity
    public static void requirePermission(RoomMemberEntity member, RoomMemberRoleEnum... allowedRoles) {
        if (member.getStatus() != com.synkork.backend.modules.roomMember.enums.MemberStatusEnum.ACTIVE) {
            throw new RuntimeException("Không có quyền");
        }
        boolean hasPermission = Arrays.stream(allowedRoles)
                .anyMatch(role -> role == member.getRole());
        if (!hasPermission) {
            throw new RuntimeException("Không có quyền");
        }
    }

    // Overload 2: Chỉ có UUID, không cần entity
    public static void requirePermission(UUID roomId, UUID userId, RoomMemberRoleEnum... allowedRoles) {
        RoomMemberEntity member = roomMemberRepository
                .findByRoom_IdAndUser_Id(roomId, userId)
                .orElseThrow(() -> new RuntimeException("Không có quyền"));
        requirePermission(member, allowedRoles);
    }
}
