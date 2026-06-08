package com.synkork.backend.modules.admin.room;

import com.synkork.backend.modules.admin.room.dto.AdminRoomDetailResponse;
import com.synkork.backend.modules.admin.room.dto.AdminRoomRequest;
import com.synkork.backend.modules.admin.room.dto.AdminRoomResponse;
import com.synkork.backend.modules.admin.room.dto.RoomFilterRequest;
import com.synkork.backend.modules.room.RoomEntity;
import com.synkork.backend.modules.room.enums.RoomStatusEnum;
import com.synkork.backend.modules.room.enums.RoomTypeEnum;
import com.synkork.backend.modules.user.UserEntity;
import com.synkork.backend.modules.user.UserRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class AdminRoomService {

    private final AdminRoomRepository roomRepository;
    private final UserRepository userRepository;

    public AdminRoomService(AdminRoomRepository roomRepository, UserRepository userRepository) {
        this.roomRepository = roomRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public Page<AdminRoomResponse> getRooms(RoomFilterRequest filter) {
        Specification<RoomEntity> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter.getSearch() != null && !filter.getSearch().isBlank()) {
                predicates.add(
                    cb.like(cb.lower(root.get("name")),
                            "%" + filter.getSearch().toLowerCase() + "%")
                );
            }

            predicates.add(cb.equal(root.get("type"), RoomTypeEnum.GROUP));

            query.distinct(true);
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Pageable pageable = PageRequest.of(
            filter.getPage(),
            filter.getSize(),
            Sort.by(Sort.Direction.DESC, "createdAt")
        );

        return roomRepository.findAll(spec, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public AdminRoomDetailResponse getRoomDetail(String roomId) {
        RoomEntity room = roomRepository.findDetailById(UUID.fromString(roomId))
                .orElseThrow(() -> new RuntimeException("Room not found: " + roomId));
        return new AdminRoomDetailResponse(room);
    }

    @Transactional
    public AdminRoomResponse createRoom(AdminRoomRequest request) {
        UserEntity owner = userRepository.findById(UUID.fromString(request.getOwnerId()))
                .orElseThrow(() -> new RuntimeException("User not found: " + request.getOwnerId()));

        RoomEntity room = RoomEntity.builder()
                .name(request.getName())
                .description(request.getDescription())
                .type(RoomTypeEnum.GROUP)
                .status(request.getStatus() != null
                        ? RoomStatusEnum.valueOf(request.getStatus().toUpperCase())
                        : RoomStatusEnum.OPEN)
                .owner(owner)
                .build();

        return toResponse(roomRepository.save(room));
    }

    @Transactional
    public AdminRoomResponse updateRoom(String roomId, AdminRoomRequest request) {
        RoomEntity room = roomRepository.findById(UUID.fromString(roomId))
                .orElseThrow(() -> new RuntimeException("Room not found: " + roomId));

        if (request.getName() != null) room.setName(request.getName());
        if (request.getDescription() != null) room.setDescription(request.getDescription());
        if (request.getStatus() != null) {
            room.setStatus(RoomStatusEnum.valueOf(request.getStatus().toUpperCase()));
        }
        if (request.getOwnerId() != null) {
            UserEntity owner = userRepository.findById(UUID.fromString(request.getOwnerId()))
                    .orElseThrow(() -> new RuntimeException("User not found: " + request.getOwnerId()));
            room.setOwner(owner);
        }

        return toResponse(roomRepository.save(room));
    }

    @Transactional
    public void deleteRoom(String roomId) {
        RoomEntity room = roomRepository.findById(UUID.fromString(roomId))
                .orElseThrow(() -> new RuntimeException("Room not found: " + roomId));
        roomRepository.delete(room);
    }

    private AdminRoomResponse toResponse(RoomEntity room) {
        return new AdminRoomResponse(room);
    }
}