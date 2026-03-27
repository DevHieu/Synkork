package com.synkork.backend.modules.roomMember;

import com.synkork.backend.modules.room.RoomEntity;
import com.synkork.backend.modules.roomMember.enums.RoomMemberRoleEnum;
import com.synkork.backend.modules.room.RoomRepository;
import com.synkork.backend.modules.user.UserEntity;
import com.synkork.backend.modules.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    public List<RoomMemberEntity> getRoomMembers(String roomId) {
        return roomMemberRepository.findByRoom_Id(UUID.fromString(roomId));
    }
}
