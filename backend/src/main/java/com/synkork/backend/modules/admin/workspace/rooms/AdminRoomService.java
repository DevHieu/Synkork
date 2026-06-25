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
        request.validate();

        Specification<RoomEntity> spec = RoomSpecification.filter(request);

        Pageable pageable = PageRequest.of(
                request.getPage(),
                request.getSize(),
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        return roomRepository.findAll(spec, pageable);
    }

    public AdminRoomDetailResponse getRoomDetail(String roomId) {
        RoomEntity room = findRoomOrThrow(roomId);
        return new AdminRoomDetailResponse(room);
    }

    public List<AdminRoomMemberResponse> getRoomMembers(String roomId) {
        RoomEntity room = findRoomOrThrow(roomId);
        return room.getRoomMembers().stream()
                .map(AdminRoomMemberResponse::new)
                .toList();
    }

    public List<AdminRoomSpaceResponse> getRoomSpaces(String roomId) {
        RoomEntity room = findRoomOrThrow(roomId);
        return room.getSpaces().stream()
                .map(AdminRoomSpaceResponse::new)
                .toList();
    }

    public AdminRoomResponse createRoom(AdminRoomRequest request) {
        if (request.name() == null || request.name().isBlank()) {
            throw new IllegalArgumentException("Tên room không được để trống");
        }

        UserEntity owner;
        if (request.ownerId() != null) {
            owner = userRepository.findById(request.ownerId())
                    .orElseThrow(() -> new RuntimeException("Owner not found: " + request.ownerId()));
        } else {
            owner = userRepository.findById(AuthUtils.getCurrentUserId())
                    .orElseThrow(() -> new RuntimeException("User not found"));
        }

        RoomEntity room = RoomEntity.builder()
                .name(request.name().trim())
                .description(request.description())
                .avatarUrl(request.avatarUrl())
                .type(RoomTypeEnum.GROUP)
                .status(request.status() != null ? request.status() : RoomStatusEnum.OPEN)
                .owner(owner)
                .build();

        return new AdminRoomResponse(roomRepository.save(room));
    }

    public List<com.synkork.backend.modules.admin.workspace.rooms.dtos.AdminUserOptionResponse> searchUserOptions(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return List.of();
        }
        return userRepository
                .findTop10ByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(keyword, keyword)
                .stream()
                .map(com.synkork.backend.modules.admin.workspace.rooms.dtos.AdminUserOptionResponse::new)
                .toList();
    }
    
    public AdminRoomResponse updateRoom(String roomId, AdminRoomRequest request) {
        RoomEntity room = findRoomOrThrow(roomId);

        if (room.getType() == RoomTypeEnum.DM) {
            if (request.status() != null) {
                room.setStatus(request.status());
            }
            return new AdminRoomResponse(roomRepository.save(room));
        }

        if (request.name() != null && !request.name().isBlank()) {
            room.setName(request.name().trim());
        }
        if (request.description() != null) {
            room.setDescription(request.description());
        }
        if (request.avatarUrl() != null) {
            room.setAvatarUrl(request.avatarUrl());
        }
        if (request.status() != null) {
            room.setStatus(request.status());
        }
        if (request.ownerId() != null && !request.ownerId().equals(
                room.getOwner() != null ? room.getOwner().getId() : null)) {
            UserEntity newOwner = userRepository.findById(request.ownerId())
                    .orElseThrow(() -> new RuntimeException("Owner not found: " + request.ownerId()));
            room.setOwner(newOwner);
        }

        return new AdminRoomResponse(roomRepository.save(room));
    }

    @Transactional
    public void deleteRoom(String roomId) {
        RoomEntity room = findRoomOrThrow(roomId);

        if (room.getType() == RoomTypeEnum.DM) {
            throw new IllegalArgumentException("Không thể xóa room loại DM từ trang quản trị");
        }

        roomRepository.delete(room);
    }

    public AdminRoomResponse warnRoom(UUID roomId) {
        RoomEntity room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        room.setWarning(room.getWarning() + 1);

        return new AdminRoomResponse(roomRepository.save(room));
    }

    private RoomEntity findRoomOrThrow(String roomId) {
        return roomRepository.findById(UUID.fromString(roomId))
                .orElseThrow(() -> new RuntimeException("Room not found: " + roomId));
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