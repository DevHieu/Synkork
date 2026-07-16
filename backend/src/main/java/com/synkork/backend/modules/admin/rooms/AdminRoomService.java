package com.synkork.backend.modules.admin.rooms;

import java.util.List;
import java.util.UUID;

import com.synkork.backend.common.utils.PlanLimitUtils;
import com.synkork.backend.modules.admin.rooms.dtos.*;
import com.synkork.backend.modules.room.RoomService;
import com.synkork.backend.modules.roomMember.RoomMemberEntity;
import com.synkork.backend.modules.roomMember.RoomMemberRepository;
import com.synkork.backend.modules.roomMember.RoomMemberService;
import com.synkork.backend.modules.space.SpaceEntity;
import com.synkork.backend.modules.space.SpaceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.synkork.backend.modules.admin.rooms.email.AdminRoomEmailService;
import com.synkork.backend.modules.room.RoomEntity;
import com.synkork.backend.modules.room.enums.RoomStatusEnum;
import com.synkork.backend.modules.room.enums.RoomTypeEnum;
import com.synkork.backend.modules.user.UserEntity;
import com.synkork.backend.modules.user.UserRepository;

@Service
public class AdminRoomService {

    @Autowired
    private AdminRoomRepository adminRoomRepository;

    @Autowired
    private RoomService roomService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoomMemberRepository roomMemberRepository;

    @Autowired
    private SpaceRepository spaceRepository;

    @Autowired
    private AdminRoomEmailService adminRoomEmailService;
    @Autowired
    private RoomMemberService roomMemberService;

    public Page<RoomEntity> getRooms(RoomFilterRequest request) {
        request.validate();

        Specification<RoomEntity> spec = RoomSpecification.filter(request);

        Pageable pageable = PageRequest.of(
                request.getPage(),
                request.getSize(),
                Sort.by(Sort.Direction.DESC, "createdAt"));

        return adminRoomRepository.findAll(spec, pageable);
    }

    public AdminRoomDetailResponse getRoomDetail(String roomId) {
        RoomEntity room = findRoomOrThrow(roomId);
        UserEntity owner = roomService.findOwnerByRoomId(room.getId());

        long memberCount = roomMemberRepository.countByRoom_Id(room.getId());
        long spaceCount = spaceRepository.countByRoom_Id(room.getId());

        return new AdminRoomDetailResponse(room, owner, memberCount, spaceCount);
    }

    public List<AdminRoomMemberResponse> getRoomMembers(String roomId) {
        List<RoomMemberEntity> members = roomMemberRepository.findByRoom_Id(UUID.fromString(roomId));
        return members.stream()
                .map(AdminRoomMemberResponse::new)
                .toList();
    }

    public List<AdminRoomSpaceResponse> getRoomSpaces(String roomId) {
        List<SpaceEntity> spaces = spaceRepository.findByRoomIdOrderByCreatedAtDesc(UUID.fromString(roomId));
        return spaces.stream()
                .map(AdminRoomSpaceResponse::new)
                .toList();
    }

    public List<AdminUserOptionResponse> searchUserOptions(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return List.of();
        }
        return userRepository
                .findTop10ByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(keyword, keyword)
                .stream()
                .map(AdminUserOptionResponse::new)
                .toList();
    }

    public AdminRoomResponse createRoom(AdminRoomRequest request) {
        if (request.name() == null || request.name().isBlank()) {
            throw new IllegalArgumentException("Tên room không được để trống");
        }

        if (request.status() == RoomStatusEnum.PENDING_REMOVAL) {
            throw new IllegalArgumentException("Không thể đặt trạng thái Pending Removal thủ công");
        }

        UserEntity owner = userRepository.findById(request.ownerId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        // Check xem owner được chọn có quá số lượng phòng theo gói hay không
        if (!PlanLimitUtils.checkMaxRooms(owner.getCurrentPlan(), owner.getId())) {
            return null;
        }

        RoomEntity room = RoomEntity.builder()
                .name(request.name().trim())
                .description(request.description())
                .avatarUrl(request.avatarUrl())
                .type(RoomTypeEnum.GROUP)
                .status(request.status() != null ? request.status() : RoomStatusEnum.OPEN)
                .owner(owner)
                .build();

        RoomEntity saved = adminRoomRepository.save(room);
        adminRoomEmailService.sendRoomCreatedEmail(saved, owner);

        return new AdminRoomResponse(saved);
    }

    public AdminRoomResponse updateRoom(String roomId, AdminRoomRequest request) {
        RoomEntity room = findRoomOrThrow(roomId);

        if (request.status() == RoomStatusEnum.PENDING_REMOVAL) {
            throw new IllegalArgumentException("Không thể đặt trạng thái Pending Removal thủ công");
        }

        // Room DM: chỉ cho đổi status
        if (room.getType() == RoomTypeEnum.DM) {
            if (request.status() != null) {
                room.setStatus(request.status());
            }
            RoomEntity saved = adminRoomRepository.save(room);
            if (saved.getOwner() != null) {
                adminRoomEmailService.sendRoomUpdatedEmail(saved, saved.getOwner());
            }
            return new AdminRoomResponse(saved);
        }

        // Room GROUP
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

        UserEntity oldOwner = room.getOwner();
        UserEntity newOwner = null;
        boolean ownerChanged = false;

        if (request.ownerId() != null && !request.ownerId().equals(
                room.getOwner() != null ? room.getOwner().getId() : null)) {

            newOwner = userRepository.findById(request.ownerId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy owner: " + request.ownerId()));

            // Check xem owner được chọn có quá số lượng phòng theo gói hay không
            if (!PlanLimitUtils.checkMaxRooms(newOwner.getCurrentPlan(), newOwner.getId())) {
                return null;
            }

            room.setOwner(newOwner);
            ownerChanged = true;
        }

        RoomEntity saved = adminRoomRepository.save(room);

        if (ownerChanged) {
            if (oldOwner != null) {
                roomMemberService.deleteMember(oldOwner.getId(), room.getId());

                adminRoomEmailService.sendRoomOwnerTransferredFromEmail(
                        oldOwner.getEmail(),
                        oldOwner.getUsername(),
                        room.getName(),
                        newOwner.getUsername(),
                        newOwner.getEmail()
                );
            }
            adminRoomEmailService.sendRoomOwnerTransferredFromEmail(
                    newOwner.getEmail(),
                    newOwner.getUsername(),
                    room.getName(),
                    oldOwner.getUsername(),
                    oldOwner.getEmail()
            );
        } else {
            if (saved.getOwner() != null) {
                adminRoomEmailService.sendRoomUpdatedEmail(saved, saved.getOwner());
            }
        }

        return new AdminRoomResponse(saved);
    }

    public AdminRoomResponse warnRoom(UUID roomId) {
        RoomEntity room = roomService.findById(roomId);

        room.setWarning(room.getWarning() + 1);

        RoomEntity saved = adminRoomRepository.save(room);
        UserEntity owner = saved.getOwner();
        if (owner != null) {
            adminRoomEmailService.sendRoomWarningEmail(saved, owner, saved.getWarning());
        }

        return new AdminRoomResponse(saved);
    }

    public AdminRoomResponse toggleRoomStatus(UUID roomId, ToggleRoomStatusRequest request) {
        if (request.status() == null) {
            throw new IllegalArgumentException("Status không được để trống");
        }

        if (request.status() == RoomStatusEnum.PENDING_REMOVAL) {
            throw new IllegalArgumentException("Không thể đặt trạng thái Pending Removal thủ công");
        }

        RoomEntity room = roomService.findById(roomId);

        if (room.getStatus() == RoomStatusEnum.PENDING_REMOVAL) {
            throw new RuntimeException("Room đang chờ xóa tự động, không thể khóa/mở khóa");
        }

        // Chỉ chặn khi cố khóa một room ĐÃ khóa rồi.
        // Khi mở khóa (status = OPEN) thì luôn cho phép, kể cả khi room đang LOCKED.
        if (request.status() == RoomStatusEnum.LOCKED && room.getStatus() == RoomStatusEnum.LOCKED) {
            throw new RuntimeException("Room đã bị khóa rồi!");
        }

        if (request.status() == RoomStatusEnum.OPEN && room.getStatus() == RoomStatusEnum.OPEN) {
            throw new RuntimeException("Room đang mở rồi!");
        }

        UserEntity owner = roomService.findOwnerByRoomId(roomId);
        if (request.status() == RoomStatusEnum.OPEN && !PlanLimitUtils.checkMaxRooms(owner.getCurrentPlan(), owner.getId())) {
            return null;
        }

        room.setStatus(request.status());
        RoomEntity saved = adminRoomRepository.save(room);

        if (saved.getOwner() != null) {
            if (request.status() == RoomStatusEnum.LOCKED) {
                adminRoomEmailService.sendRoomLockedEmail(saved, saved.getOwner(), request.reason());
            } else if (request.status() == RoomStatusEnum.OPEN) {
                adminRoomEmailService.sendRoomUnlockedEmail(saved, saved.getOwner());
            }
        }

        return new AdminRoomResponse(saved);
    }

    private RoomEntity findRoomOrThrow(String roomId) {
        return roomService.findById(UUID.fromString(roomId));
    }
}