package com.synkork.backend.modules.admin.rooms;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.synkork.backend.common.utils.AuthUtils;
import com.synkork.backend.common.utils.PlanLimitUtils;
import com.synkork.backend.modules.admin.auditLog.AuditLogService;
import com.synkork.backend.modules.admin.auditLog.dtos.BuildLog;
import com.synkork.backend.modules.admin.auditLog.enums.LogActionEnum;
import com.synkork.backend.modules.admin.auditLog.enums.LogEntityTypeEnum;
import com.synkork.backend.modules.admin.rooms.dtos.*;
import com.synkork.backend.modules.admin.utils.AdminUtils;
import com.synkork.backend.modules.room.RoomService;
import com.synkork.backend.modules.roomMember.RoomMemberEntity;
import com.synkork.backend.modules.roomMember.RoomMemberRepository;
import com.synkork.backend.modules.roomMember.RoomMemberService;
import com.synkork.backend.modules.roomMember.enums.MemberStatusEnum;
import com.synkork.backend.modules.roomMember.enums.RoomMemberRoleEnum;
import com.synkork.backend.modules.space.SpaceEntity;
import com.synkork.backend.modules.space.SpaceRepository;
import com.synkork.backend.modules.user.enums.RoleEnum;
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

    @Autowired
    private AuditLogService auditLogService;

    @Autowired
    private ObjectMapper objectMapper;

    public RoomDashboardStatsResponse getRoomStats(LocalDateTime dateFrom, LocalDateTime dateTo) {
        LocalDateTime effectiveTo = dateTo != null ? dateTo : LocalDateTime.now();
        LocalDateTime effectiveFrom = dateFrom != null ? dateFrom : effectiveTo.minusMonths(1);

        long totalRooms = adminRoomRepository.countByTypeAndCreatedAtLessThanEqual(RoomTypeEnum.GROUP, effectiveTo);
        double roomGrowth = this.calculateRoomGrowth(effectiveFrom, effectiveTo, totalRooms);

        long newRooms = adminRoomRepository.countByTypeAndCreatedAtBetween(RoomTypeEnum.GROUP, effectiveFrom, effectiveTo);

        long groupRooms = adminRoomRepository.countByTypeAndCreatedAtBetween(RoomTypeEnum.GROUP, effectiveFrom, effectiveTo);
        long membersInGroupRooms = roomMemberRepository.countByRoomTypeAndRoomCreatedAtBetween(
                RoomTypeEnum.GROUP,
                effectiveFrom,
                effectiveTo
        );
        double averageMembersPerRoom = groupRooms == 0
                ? 0.0
                : Math.round(((double) membersInGroupRooms / groupRooms) * 10.0) / 10.0;

        long warnedRooms = adminRoomRepository.countByWarningGreaterThanAndCreatedAtBetweenAndType(0, effectiveFrom, effectiveTo, RoomTypeEnum.GROUP);

        return RoomDashboardStatsResponse.builder()
                .totalRooms(totalRooms)
                .newRooms(newRooms)
                .roomGrowth(roomGrowth)
                .averageMembersPerRoom(averageMembersPerRoom)
                .warnedRooms(warnedRooms)
                .build();
    }

    public List<RoomStatusCount> getRoomChart(LocalDateTime dateFrom, LocalDateTime dateTo) {
        return adminRoomRepository.countGroupByStatus(dateFrom, dateTo);
    }

    public double calculateRoomGrowth(LocalDateTime dateFrom, LocalDateTime dateTo, Long total) {
        long totalRooms = total != null ? total : adminRoomRepository.countByTypeAndCreatedAtLessThanEqual(RoomTypeEnum.GROUP, dateTo);;
        long previousTotalRooms = adminRoomRepository.countByTypeAndCreatedAtLessThanEqual(RoomTypeEnum.GROUP, dateFrom);
        return AdminUtils.calcGrowth(totalRooms, previousTotalRooms);
    }

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
        createLog(saved, LogActionEnum.CREATE_ROOM, null, Map.of(
                "ownerId", owner.getId().toString(),
                "ownerEmail", owner.getEmail(),
                "status", saved.getStatus().name()
        ));

        return new AdminRoomResponse(saved);
    }

    public AdminRoomResponse updateRoom(String roomId, AdminRoomRequest request) {
        RoomEntity room = findRoomOrThrow(roomId);
        String oldName = room.getName();
        String oldDescription = room.getDescription();
        RoomStatusEnum oldStatus = room.getStatus();
        UserEntity oldOwner = room.getOwner();

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
            createLog(saved, LogActionEnum.UPDATE_ROOM, null, metadata(
                    "oldStatus", oldStatus.name(),
                    "newStatus", saved.getStatus().name()
            ));
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
            ensureOwnerMember(room, newOwner);
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
                    oldOwner != null ? oldOwner.getUsername() : "Không có",
                    oldOwner != null ? oldOwner.getEmail() : ""
            );
        } else {
            if (saved.getOwner() != null) {
                adminRoomEmailService.sendRoomUpdatedEmail(saved, saved.getOwner());
            }
        }

        createLog(saved, ownerChanged ? LogActionEnum.TRANSFER_ROOM_OWNER : LogActionEnum.UPDATE_ROOM, null, metadata(
                "oldName", oldName,
                "newName", saved.getName(),
                "oldDescription", oldDescription,
                "newDescription", saved.getDescription(),
                "oldStatus", oldStatus.name(),
                "newStatus", saved.getStatus().name(),
                "oldOwnerId", oldOwner != null ? oldOwner.getId().toString() : null,
                "newOwnerId", saved.getOwner() != null ? saved.getOwner().getId().toString() : null
        ));

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
        createLog(saved, LogActionEnum.WARN_ROOM, null, Map.of(
                "warning", saved.getWarning()
        ));

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

        RoomStatusEnum oldStatus = room.getStatus();
        room.setStatus(request.status());
        RoomEntity saved = adminRoomRepository.save(room);

        if (saved.getOwner() != null) {
            if (request.status() == RoomStatusEnum.LOCKED) {
                adminRoomEmailService.sendRoomLockedEmail(saved, saved.getOwner(), request.reason());
            } else if (request.status() == RoomStatusEnum.OPEN) {
                adminRoomEmailService.sendRoomUnlockedEmail(saved, saved.getOwner());
            }
        }

        createLog(saved, request.status() == RoomStatusEnum.LOCKED ? LogActionEnum.LOCK_ROOM : LogActionEnum.UNLOCK_ROOM, request.reason(), Map.of(
                "oldStatus", oldStatus.name(),
                "newStatus", saved.getStatus().name()
        ));

        return new AdminRoomResponse(saved);
    }

    private void ensureOwnerMember(RoomEntity room, UserEntity owner) {
        roomMemberRepository.findByRoom_IdAndUser_Id(room.getId(), owner.getId())
                .ifPresentOrElse(member -> {
                    member.setStatus(MemberStatusEnum.ACTIVE);
                    member.setRole(RoomMemberRoleEnum.OWNER);
                    roomMemberRepository.save(member);
                }, () -> roomMemberService.addRoomMembers(
                        owner.getId(),
                        room.getId().toString(),
                        RoomMemberRoleEnum.OWNER.name()
                ));
    }

    private Map<String, Object> metadata(Object... keyValues) {
        Map<String, Object> result = new HashMap<>();
        for (int i = 0; i + 1 < keyValues.length; i += 2) {
            result.put(String.valueOf(keyValues[i]), keyValues[i + 1]);
        }
        return result;
    }

    private void createLog(RoomEntity room, LogActionEnum action, String reason, Map<String, Object> metadata) {
        BuildLog log = BuildLog.builder()
                .action(action)
                .entityType(LogEntityTypeEnum.ROOM)
                .entityId(room.getId().toString())
                .entityName(room.getName())
                .description(AuthUtils.getCurrentUsername() + " performed " + action.name() + " for room " + room.getName())
                .metadata(writeMetadata(metadataWithReason(metadata, reason)))
                .build();

        auditLogService.log(log);
    }

    private Map<String, Object> metadataWithReason(Map<String, Object> metadata, String reason) {
        Map<String, Object> result = new HashMap<>(metadata);
        if (reason != null && !reason.isBlank()) {
            result.put("reason", reason);
        }
        return result;
    }

    private String writeMetadata(Map<String, Object> metadata) {
        try {
            return objectMapper.writeValueAsString(metadata);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize audit metadata", e);
        }
    }

    private RoomEntity findRoomOrThrow(String roomId) {
        return roomService.findById(UUID.fromString(roomId));
    }
}
