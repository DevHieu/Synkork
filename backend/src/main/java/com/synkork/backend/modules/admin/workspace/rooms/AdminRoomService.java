package com.synkork.backend.modules.admin.workspace.rooms;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.synkork.backend.common.utils.AuthUtils;
import com.synkork.backend.common.utils.EmailService;
import com.synkork.backend.modules.admin.workspace.members.dtos.AdminRoomMemberResponse;
import com.synkork.backend.modules.admin.workspace.rooms.dtos.AdminRoomDetailResponse;
import com.synkork.backend.modules.admin.workspace.rooms.dtos.AdminRoomRequest;
import com.synkork.backend.modules.admin.workspace.rooms.dtos.AdminRoomResponse;
import com.synkork.backend.modules.admin.workspace.rooms.dtos.AdminUserOptionResponse;
import com.synkork.backend.modules.admin.workspace.rooms.dtos.RoomFilterRequest;
import com.synkork.backend.modules.admin.workspace.spaces.dtos.AdminRoomSpaceResponse;
import com.synkork.backend.modules.room.RoomEntity;
import com.synkork.backend.modules.room.enums.RoomStatusEnum;
import com.synkork.backend.modules.room.enums.RoomTypeEnum;
import com.synkork.backend.modules.roomMember.enums.RoomMemberStatusEnum;
import com.synkork.backend.modules.user.UserEntity;
import com.synkork.backend.modules.user.UserRepository;

@Service
public class AdminRoomService {

    @Autowired
    private AdminRoomRepository roomRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    public Page<RoomEntity> getRooms(RoomFilterRequest request) {
        request.validate();

        Specification<RoomEntity> spec = RoomSpecification.filter(request);

        Pageable pageable = PageRequest.of(
                request.getPage(),
                request.getSize(),
                Sort.by(Sort.Direction.DESC, "createdAt"));

        return roomRepository.findAll(spec, pageable);
    }

    public AdminRoomDetailResponse getRoomDetail(String roomId) {
        RoomEntity room = findRoomOrThrow(roomId);
        return new AdminRoomDetailResponse(room);
    }

    public List<AdminRoomMemberResponse> getRoomMembers(String roomId) {
        RoomEntity room = findRoomOrThrow(roomId);
        return room.getRoomMembers().stream()
                .filter(member -> member.getStatus() == RoomMemberStatusEnum.ACTIVE)
                .map(AdminRoomMemberResponse::new)
                .toList();
    }

    public List<AdminRoomSpaceResponse> getRoomSpaces(String roomId) {
        RoomEntity room = findRoomOrThrow(roomId);
        return room.getSpaces().stream()
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

    // ─── TẠO MỚI ─────────────────────────────────────────────────────────────

    public AdminRoomResponse createRoom(AdminRoomRequest request) {
        if (request.name() == null || request.name().isBlank()) {
            throw new IllegalArgumentException("Tên room không được để trống");
        }

        if (request.status() == RoomStatusEnum.PENDING_REMOVAL) {
            throw new IllegalArgumentException("Không thể đặt trạng thái Pending Removal thủ công");
        }

        UserEntity owner;
        if (request.ownerId() != null) {
            owner = userRepository.findById(request.ownerId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy owner: " + request.ownerId()));
        } else {
            owner = userRepository.findById(AuthUtils.getCurrentUserId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));
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

    // ─── CẬP NHẬT ────────────────────────────────────────────────────────────

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
            AdminRoomResponse saved = new AdminRoomResponse(roomRepository.save(room));
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
            room.setOwner(newOwner);
        }

        AdminRoomResponse saved = new AdminRoomResponse(roomRepository.save(room));
        sendOwnerUpdateEmail(room);
        return saved;
    }

    // ─── CẢNH BÁO ────────────────────────────────────────────────────────────

    public AdminRoomResponse warnRoom(UUID roomId) {
        RoomEntity room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy room"));

        room.setWarning(room.getWarning() + 1);

        RoomEntity saved = roomRepository.save(room);
        UserEntity owner = saved.getOwner();
        if (owner != null) {
            emailService.sendWarningEmail(owner.getEmail(), saved.getName(), "phòng của bạn", saved.getWarning());
        }

        return new AdminRoomResponse(saved);
    }

    // ─── KHÓA / MỞ KHÓA ──────────────────────────────────────────────────────

    public AdminRoomResponse lockRoom(UUID roomId, RoomStatusEnum status) {
        if (status == null) {
            throw new IllegalArgumentException("Status không được để trống");
        }

        if (status == RoomStatusEnum.PENDING_REMOVAL) {
            throw new IllegalArgumentException("Không thể đặt trạng thái Pending Removal thủ công");
        }

        RoomEntity room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy room: " + roomId));

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
        RoomEntity saved = roomRepository.save(room);

        if (saved.getOwner() != null) {
            if (status == RoomStatusEnum.LOCKED) {
                emailService.sendLockEmail(saved.getOwner().getEmail(), saved.getName(), "phòng của bạn");
            } else if (status == RoomStatusEnum.OPEN) {
                sendOwnerUnlockEmail(saved);
            }
        }

        return new AdminRoomResponse(saved);
    }

    // ─── Private helpers ─────────────────────────────────────────────────────

    private RoomEntity findRoomOrThrow(String roomId) {
        return roomRepository.findById(UUID.fromString(roomId))
                .orElseThrow(() -> new RuntimeException("Không tìm thấy room: " + roomId));
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
