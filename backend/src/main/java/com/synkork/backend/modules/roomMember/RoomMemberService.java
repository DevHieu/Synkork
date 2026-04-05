package com.synkork.backend.modules.roomMember;

import com.synkork.backend.modules.roomMember.dto.ChangeAuthorityDTO;
import com.synkork.backend.modules.room.RoomEntity;
import com.synkork.backend.modules.roomMember.dto.RoomMemberDto;
import com.synkork.backend.modules.roomMember.enums.RoomMemberRoleEnum;
import com.synkork.backend.modules.room.RoomRepository;
import com.synkork.backend.modules.user.UserEntity;
import com.synkork.backend.modules.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
public class RoomMemberService {

    @Autowired
    RoomRepository roomRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    RoomMemberRepository roomMemberRepository;


    // Hàm này để lấy ra thằng gửi request. Check xem nó có đúng cái quyền dduwwocj cho phép chưa. Chưa thì cúc
    public RoomMemberEntity getMemberWithAuthority(UUID roomId, UUID userId, RoomMemberRoleEnum... allowedRoles) {
        RoomMemberEntity requester = roomMemberRepository
                .findByRoom_IdAndUser_Id(roomId, userId)
                .orElseThrow(() -> new RuntimeException("Không có quyền"));

        boolean hasPermission = Arrays.stream(allowedRoles)
                .anyMatch(role -> role == requester.getRole());

        if (!hasPermission) {
            throw new RuntimeException("Không có quyền");
        }

        return requester;
    }

    public RoomMemberEntity addRoomMembers(String userId, String roomID, String role) {
        RoomMemberEntity roomMemberEntity = new RoomMemberEntity();

        RoomEntity room = roomRepository.findById(UUID.fromString(roomID))
                .orElseThrow(() -> new RuntimeException("Room not found: " + roomID));

        UserEntity user = userRepository.findById(UUID.fromString(userId))
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        roomMemberEntity.setRoom(room);
        roomMemberEntity.setUser(user);

        try {
            roomMemberEntity.setRole(role != null
                    ? RoomMemberRoleEnum.valueOf(role.toUpperCase())
                    : RoomMemberRoleEnum.MEMBER);
        } catch (IllegalArgumentException e) {
            roomMemberEntity.setRole(RoomMemberRoleEnum.MEMBER);
        }

        return roomMemberRepository.save(roomMemberEntity);
    }

    public List<RoomMemberDto> getRoomMembers(String roomId) {
        return roomMemberRepository.findByRoom_Id(UUID.fromString(roomId))
                .stream()
                .map(RoomMemberDto::new)
                .toList();
    }

    public RoomMemberEntity changerAuthority(ChangeAuthorityDTO dto, UUID roomId, UUID requesterUserId) {

       this.getMemberWithAuthority(roomId, requesterUserId, RoomMemberRoleEnum.OWNER);

        RoomMemberEntity member = roomMemberRepository.findById(UUID.fromString(dto.memberId())).orElseThrow(() -> new RuntimeException("Không tìm thấy member"));

        if (member.getRole() == RoomMemberRoleEnum.OWNER) {
            throw new RuntimeException("Không thể đổi quyền chủ phòng");
        }

        member.setRole(RoomMemberRoleEnum.valueOf(dto.newRole()));
        return roomMemberRepository.save(member);
    }

    public String kickMember(UUID memberUUID, UUID roomUUID, UUID userId) {
        RoomMemberEntity kicker = getMemberWithAuthority(roomUUID, userId,
                RoomMemberRoleEnum.OWNER, RoomMemberRoleEnum.ADMIN);

        RoomMemberEntity target = roomMemberRepository.findById(memberUUID)
                .orElseThrow(() -> new RuntimeException("Member not found"));

        if (target.getRole() == RoomMemberRoleEnum.OWNER) {
            throw new RuntimeException("Cannot kick OWNER");
        }

        if (kicker.getRole() == RoomMemberRoleEnum.ADMIN
                && target.getRole() == RoomMemberRoleEnum.ADMIN) {
            throw new RuntimeException("ADMIN cannot kick another ADMIN");
        }

        roomMemberRepository.deleteById(memberUUID);

        return target.getUser().getEmail();
    }
}
