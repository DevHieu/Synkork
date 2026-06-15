package com.synkork.backend.modules.admin.workspace.rooms;

import com.synkork.backend.common.utils.AuthUtils;
import com.synkork.backend.modules.admin.workspace.members.dtos.AdminRoomMemberResponse;
import com.synkork.backend.modules.admin.workspace.rooms.dtos.AdminRoomDetailResponse;
import com.synkork.backend.modules.admin.workspace.rooms.dtos.AdminRoomRequest;
import com.synkork.backend.modules.admin.workspace.rooms.dtos.AdminRoomResponse;
import com.synkork.backend.modules.admin.workspace.rooms.dtos.RoomFilterRequest;
import com.synkork.backend.modules.admin.workspace.spaces.dtos.AdminRoomSpaceResponse;
import com.synkork.backend.modules.room.RoomEntity;
import com.synkork.backend.modules.room.enums.RoomStatusEnum;
import com.synkork.backend.modules.room.enums.RoomTypeEnum;
import com.synkork.backend.modules.user.UserEntity;
import com.synkork.backend.modules.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class AdminRoomService {

    @Autowired
    private AdminRoomRepository roomRepository;

    @Autowired
    private UserRepository userRepository;

    public Page<RoomEntity> getRooms(RoomFilterRequest request) {

        request.validate(); // validate dateFrom and dateTo

        Specification<RoomEntity> spec = RoomSpecification.filter(request);

        Pageable pageable = PageRequest.of(
                request.getPage(),
                request.getSize(),
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        return roomRepository.findAll(spec, pageable);
    }

    public AdminRoomDetailResponse getRoomDetail(String roomId) {
        RoomEntity room = roomRepository.findById(UUID.fromString(roomId))
                .orElseThrow(() -> new RuntimeException("Room not found: " + roomId));
        return new AdminRoomDetailResponse(room);
    }

    public List<AdminRoomMemberResponse> getRoomMembers(String roomId) {
        RoomEntity room = roomRepository.findById(UUID.fromString(roomId))
                .orElseThrow(() -> new RuntimeException("Room not found: " + roomId));
        return room.getRoomMembers().stream()
                .map(AdminRoomMemberResponse::new)
                .toList();
    }

    public List<AdminRoomSpaceResponse> getRoomSpaces(String roomId) {
        RoomEntity room = roomRepository.findById(UUID.fromString(roomId))
                .orElseThrow(() -> new RuntimeException("Room not found: " + roomId));
        return room.getSpaces().stream()
                .map(AdminRoomSpaceResponse::new)
                .toList();
    }

    public AdminRoomResponse createRoom(AdminRoomRequest request) {
        UserEntity owner = userRepository.findById(AuthUtils.getCurrentUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        RoomEntity room = RoomEntity.builder()
                .name(request.name())
                .description(request.description())
                .type(RoomTypeEnum.GROUP)
                .status(request.status() != null
                        ? request.status()
                        : RoomStatusEnum.OPEN)
                .owner(owner)
                .build();

        return new AdminRoomResponse(roomRepository.save(room));
    }

    public AdminRoomResponse updateRoom(String roomId, AdminRoomRequest request) {
        RoomEntity room = roomRepository.findById(UUID.fromString(roomId))
                .orElseThrow(() -> new RuntimeException("Room not found: " + roomId));

        if (request.name() != null) room.setName(request.name());
        if (request.description() != null) room.setDescription(request.description());
        if (request.status() != null) {
            room.setStatus(request.status());
        }

        return new AdminRoomResponse(roomRepository.save(room));
    }

    @Transactional
    public void deleteRoom(String roomId) {
        RoomEntity room = roomRepository.findById(UUID.fromString(roomId))
                .orElseThrow(() -> new RuntimeException("Room not found: " + roomId));
        roomRepository.delete(room);
    }

    public AdminRoomResponse lockRoom(UUID roomId, RoomStatusEnum status){
        RoomEntity room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found: " + roomId));

        if(room.getStatus() == RoomStatusEnum.LOCKED){
                throw new RuntimeException("Room already locked!");
        }
        
        room.setStatus(status);
        return new AdminRoomResponse(roomRepository.save(room));
    }

}