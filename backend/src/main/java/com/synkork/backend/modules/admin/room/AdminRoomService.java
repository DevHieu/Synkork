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
import jakarta.persistence.criteria.Subquery;
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

    public Page<AdminRoomResponse> getRooms(RoomFilterRequest filter) {
        Specification<RoomEntity> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Chỉ lấy GROUP
            predicates.add(cb.equal(root.get("type"), RoomTypeEnum.GROUP));

            // Filter theo tên
            if (filter.getSearch() != null && !filter.getSearch().isBlank()) {
                predicates.add(
                    cb.like(cb.lower(root.get("name")),
                            "%" + filter.getSearch().toLowerCase() + "%")
                );
            }

            // Filter theo status
            if (filter.getStatus() != null && !filter.getStatus().isBlank()) {
                try {
                    RoomStatusEnum statusEnum = RoomStatusEnum.valueOf(filter.getStatus().toUpperCase());
                    predicates.add(cb.equal(root.get("status"), statusEnum));
                } catch (IllegalArgumentException ignored) {}
            }

            // Filter theo ngày tạo từ
            if (filter.getCreatedFrom() != null) {
                predicates.add(cb.greaterThanOrEqualTo(
                    root.get("createdAt"),
                    filter.getCreatedFrom().atStartOfDay()
                ));
            }

            // Filter theo ngày tạo đến
            if (filter.getCreatedTo() != null) {
                predicates.add(cb.lessThanOrEqualTo(
                    root.get("createdAt"),
                    filter.getCreatedTo().atTime(23, 59, 59)
                ));
            }

            // Filter theo số member tối thiểu
            if (filter.getMinMembers() != null && filter.getMinMembers() > 0) {
                Subquery<Long> subquery = query.subquery(Long.class);
                var memberRoot = subquery.from(com.synkork.backend.modules.roomMember.RoomMemberEntity.class);
                subquery.select(cb.count(memberRoot))
                        .where(cb.equal(memberRoot.get("room"), root));
                predicates.add(cb.greaterThanOrEqualTo(subquery, (long) filter.getMinMembers()));
            }

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