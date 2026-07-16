package com.synkork.backend.modules.admin.rooms;

import java.util.List;
import java.util.UUID;

import com.synkork.backend.common.utils.PlanLimitUtils;
import com.synkork.backend.modules.room.RoomService;
import com.synkork.backend.modules.roomMember.RoomMemberEntity;
import com.synkork.backend.modules.roomMember.RoomMemberRepository;
import com.synkork.backend.modules.roomMember.RoomMemberService;
import com.synkork.backend.modules.roomMember.enums.RoomMemberRoleEnum;
import com.synkork.backend.modules.space.SpaceEntity;
import com.synkork.backend.modules.space.SpaceRepository;
import com.synkork.backend.modules.user.enums.PlanEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.synkork.backend.common.utils.AuthUtils;
import com.synkork.backend.common.utils.EmailService;
import com.synkork.backend.modules.admin.rooms.dtos.AdminRoomMemberResponse;
import com.synkork.backend.modules.admin.rooms.dtos.AdminRoomDetailResponse;
import com.synkork.backend.modules.admin.rooms.dtos.AdminRoomRequest;
import com.synkork.backend.modules.admin.rooms.dtos.AdminRoomResponse;
import com.synkork.backend.modules.admin.rooms.dtos.AdminUserOptionResponse;
import com.synkork.backend.modules.admin.rooms.dtos.RoomFilterRequest;
import com.synkork.backend.modules.admin.rooms.dtos.AdminRoomSpaceResponse;
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
    private EmailService emailService;

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

        return new AdminRoomResponse(adminRoomRepository.save(room));
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
            AdminRoomResponse saved = new AdminRoomResponse(adminRoomRepository.save(room));
            sendOwnerUpdateEmail(room);
            return saved;
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
        if (request.ownerId() != null && !request.ownerId().equals(
                room.getOwner() != null ? room.getOwner().getId() : null)) {

            UserEntity newOwner = userRepository.findById(request.ownerId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy owner: " + request.ownerId()));

            // Check xem owner được chọn có quá số lượng phòng theo gói hay không
            if (!PlanLimitUtils.checkMaxRooms(newOwner.getCurrentPlan(), newOwner.getId())) {
                return null;
            }

            room.setOwner(newOwner);
        }

        AdminRoomResponse saved = new AdminRoomResponse(adminRoomRepository.save(room));
        sendOwnerUpdateEmail(room);
        return saved;
    }

    public AdminRoomResponse warnRoom(UUID roomId) {
        RoomEntity room = roomService.findById(roomId);

        room.setWarning(room.getWarning() + 1);

        RoomEntity saved = adminRoomRepository.save(room);
        UserEntity owner = saved.getOwner();
        if (owner != null) {
            emailService.sendWarningEmail(owner.getEmail(), saved.getName(), "phòng của bạn", saved.getWarning());
        }

        return new AdminRoomResponse(saved);
    }

    public AdminRoomResponse lockRoom(UUID roomId, RoomStatusEnum status) {
        if (status == null) {
            throw new IllegalArgumentException("Status không được để trống");
        }

        if (status == RoomStatusEnum.PENDING_REMOVAL) {
            throw new IllegalArgumentException("Không thể đặt trạng thái Pending Removal thủ công");
        }

        RoomEntity room = roomService.findById(roomId);

        if (room.getStatus() == RoomStatusEnum.PENDING_REMOVAL) {
            throw new RuntimeException("Room đang chờ xóa tự động, không thể khóa/mở khóa");
        }

        // Chỉ chặn khi cố khóa một room ĐÃ khóa rồi.
        // Khi mở khóa (status = OPEN) thì luôn cho phép, kể cả khi room đang LOCKED.
        if (status == RoomStatusEnum.LOCKED && room.getStatus() == RoomStatusEnum.LOCKED) {
            throw new RuntimeException("Room đã bị khóa rồi!");
        }

        if (status == RoomStatusEnum.OPEN && room.getStatus() == RoomStatusEnum.OPEN) {
            throw new RuntimeException("Room đang mở rồi!");
        }

        room.setStatus(status);
        RoomEntity saved = adminRoomRepository.save(room);

        if (saved.getOwner() != null) {
            if (status == RoomStatusEnum.LOCKED) {
                emailService.sendLockEmail(saved.getOwner().getEmail(), saved.getName(), "phòng của bạn");
            } else if (status == RoomStatusEnum.OPEN) {
                sendOwnerUnlockEmail(saved);
            }
        }

        return new AdminRoomResponse(saved);
    }

    private RoomEntity findRoomOrThrow(String roomId) {
        return roomService.findById(UUID.fromString(roomId));
    }

    private void sendOwnerUpdateEmail(RoomEntity room) {
        if (room.getOwner() == null || room.getOwner().getEmail() == null)
            return;

        String ownerEmail = room.getOwner().getEmail();
        String roomName = room.getName() != null ? room.getName() : "Direct Message";

        String subject = "[Synkork] Room của bạn vừa được cập nhật";
        String body = """
                <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;
                            padding: 24px; border: 1px solid #e5e7eb; border-radius: 12px;">
                    <h2 style="color: #023c3d;">Thông báo cập nhật Room</h2>
                    <p style="color: #374151;">
                        Room <strong>%s</strong> của bạn vừa được quản trị viên cập nhật thông tin.
                    </p>
                    <div style="margin: 16px 0; padding: 16px; background: #f0fdf4;
                                border-left: 4px solid #22c55e; border-radius: 8px;">
                        <p style="margin: 0; color: #166534;">
                            ℹ️ Nếu bạn có thắc mắc về thay đổi này, vui lòng liên hệ đội ngũ hỗ trợ Synkork.
                        </p>
                    </div>
                    <hr style="border: none; border-top: 1px solid #e5e7eb; margin: 24px 0 16px;"/>
                    <p style="margin: 0; font-size: 12px; color: #9ca3af; text-align: center;">
                        Đây là email tự động từ Synkork — vui lòng không reply.
                    </p>
                </div>
                """.formatted(roomName);

        emailService.send(ownerEmail, subject, body);
    }

    private void sendOwnerUnlockEmail(RoomEntity room) {
        if (room.getOwner() == null || room.getOwner().getEmail() == null)
            return;

        String ownerEmail = room.getOwner().getEmail();
        String roomName = room.getName() != null ? room.getName() : "phòng của bạn";

        String subject = "[Synkork] Room của bạn đã được mở khóa";
        String body = """
                <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;
                            padding: 24px; border: 1px solid #e5e7eb; border-radius: 12px;">
                    <h2 style="color: #023c3d;">Thông báo mở khóa Room</h2>
                    <p style="color: #374151;">
                        Room <strong>%s</strong> của bạn đã được quản trị viên mở khóa và có thể
                        hoạt động bình thường trở lại.
                    </p>
                    <hr style="border: none; border-top: 1px solid #e5e7eb; margin: 24px 0 16px;"/>
                    <p style="margin: 0; font-size: 12px; color: #9ca3af; text-align: center;">
                        Đây là email tự động từ Synkork — vui lòng không reply.
                    </p>
                </div>
                """.formatted(roomName);

        emailService.send(ownerEmail, subject, body);
    }
}