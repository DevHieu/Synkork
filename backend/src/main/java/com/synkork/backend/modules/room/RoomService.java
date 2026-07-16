package com.synkork.backend.modules.room;

import com.synkork.backend.common.dtos.FileUploaded;
import com.synkork.backend.common.utils.AuthUtils;
import com.synkork.backend.common.utils.FileService;
import com.synkork.backend.common.utils.PermissionService;
import com.synkork.backend.modules.room.dto.CreateRoomDto;
import com.synkork.backend.modules.room.dto.RoomDto;
import com.synkork.backend.modules.room.dto.RoomReviewResponse;
import com.synkork.backend.modules.room.dto.UpdateRoomDto;
import com.synkork.backend.modules.room.enums.RoomStatusEnum;
import com.synkork.backend.modules.room.enums.RoomTypeEnum;
import com.synkork.backend.modules.roomMember.RoomMemberEntity;
import com.synkork.backend.modules.roomMember.RoomMemberRepository;
import com.synkork.backend.modules.roomMember.RoomMemberService;
import com.synkork.backend.modules.roomMember.dto.RoomMemberDto;
import com.synkork.backend.modules.roomMember.enums.RoomMemberRoleEnum;
import com.synkork.backend.modules.roomMember.enums.RoomMemberStatusEnum;
import com.synkork.backend.modules.space.SpaceEntity;
import com.synkork.backend.modules.space.SpaceService;
import com.synkork.backend.modules.space.dto.CreateSpaceRequest;
import com.synkork.backend.common.utils.PlanLimitUtils;
import com.synkork.backend.modules.user.UserEntity;
import com.synkork.backend.modules.user.UserRepository;
import com.synkork.backend.modules.user.enums.PlanEnum;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class RoomService {

    @Autowired
    RoomRepository roomRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    RoomMemberService roomMemberService;

    @Autowired
    RoomMemberRepository roomMemberRepository;

    @Autowired
    FileService imageService;

    @Autowired
    SimpMessagingTemplate messagingTemplate;

    @Autowired
    private SpaceService spaceService;

    private String generateInviteCode() {
        String chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    public RoomEntity findById(UUID uuid) {
        return roomRepository.findById(uuid).orElseThrow(() -> new RuntimeException("Room không tồn tại!"));
    }

    public RoomEntity createRoom(CreateRoomDto roomData) {
        if (roomData.ownerId() != null) {
            UUID ownerId = UUID.fromString(roomData.ownerId());
            UserEntity owner = userRepository.findById(ownerId)
                    .orElseThrow(() -> new RuntimeException("User không tồn tại"));

            PlanEnum plan = owner.getCurrentPlan();
            int maxRooms = PlanLimitUtils.maxRooms(plan);
            long currentRooms = roomMemberRepository.countGroupRoomsByUserIdAndRole(
                    ownerId, RoomMemberRoleEnum.OWNER);

            if (currentRooms >= maxRooms) {
                throw new RuntimeException(
                        "Gói " + plan + " chỉ được tạo tối đa " + maxRooms + " room. Vui lòng nâng cấp gói.");
            }
        }

        RoomEntity roomEntity = new RoomEntity();
        roomEntity.setName(roomData.name());
        roomEntity.setInviteCode(generateInviteCode());

        if (roomData.imageFile() != null) {
            FileUploaded avatar = imageService.uploadImage(roomData.imageFile(), "roomAvatar");
            roomEntity.setAvatarUrl(avatar.url());
            roomEntity.setAvatarId(avatar.publicId());
        }

        if (roomData.ownerId() != null) {
            UUID ownerId = UUID.fromString(roomData.ownerId());
            roomEntity.setOwner(userRepository.getReferenceById(ownerId));
        }

        return roomRepository.save(roomEntity);
    }

    public RoomEntity updateRoom(UUID roomId, UpdateRoomDto roomData) {

        UUID requesterId = AuthUtils.getCurrentUserId();
        PermissionService.requirePermission(roomId, requesterId, RoomMemberRoleEnum.OWNER, RoomMemberRoleEnum.ADMIN);

        RoomEntity room = this.findById(roomId);

        room.setName(roomData.name());
        room.setDescription(roomData.description());

        if (roomData.imageFile() != null) {

            // Xóa ảnh cũ
            String avatarId = room.getAvatarId();
            if (avatarId != null && !avatarId.isEmpty()) {
                imageService.deleteFile(avatarId, "image");
            }

            FileUploaded avatar = imageService.uploadImage(roomData.imageFile(), "roomAvatar");
            room.setAvatarUrl(avatar.url());
            room.setAvatarId(avatar.publicId());
        }

        return roomRepository.save(room);
    }

    public List<RoomEntity> findRoomUserJoined(@NonNull UUID userId) {
        return roomRepository.findRoomMembersJoined(userId);
    }

    public RoomReviewResponse getRoomByInviteCode(String code) {
        RoomEntity room = roomRepository.findByInviteCode(code)
                .orElseThrow(() -> new RuntimeException("Link mời không tồn tại"));

        return RoomReviewResponse.builder()
                .roomName(room.getName())
                .roomAvatar(room.getAvatarUrl())
                .roomMembers(roomMemberRepository.countByRoom_Id(room.getId()))
                .build();
    }

    public RoomDto joinRoom(String code, UUID userId) {
        RoomEntity room = roomRepository.findByInviteCode(code)
                .orElseThrow(() -> new RuntimeException("Link mời không tồn tại"));

        RoomMemberEntity existingMember = roomMemberRepository
                .findIncludingInactiveByRoomIdAndUserId(room.getId(), userId)
                .orElse(null);
        if (existingMember != null && existingMember.getStatus() == RoomMemberStatusEnum.ACTIVE)
            throw new RuntimeException("Bạn đã là thành viên của phòng này");

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));

        RoomMemberEntity member = existingMember != null ? existingMember : RoomMemberEntity.builder()
                .room(room).user(user).build();
        member.setRole(RoomMemberRoleEnum.MEMBER);
        member.setStatus(RoomMemberStatusEnum.ACTIVE);
        member.setJoinedAt(LocalDateTime.now());

        // Vừa save vừa convert sang dto luôn
        RoomMemberDto dto = new RoomMemberDto(roomMemberRepository.save(member));

        messagingTemplate.convertAndSend("/topic/room/" + room.getId() + "/members/joined", dto);

        return new RoomDto(room);
    }

    // Reset invite code
    public String resetInviteCode(String roomId) {
        RoomEntity room = this.findById(UUID.fromString(roomId));

        room.setInviteCode(generateInviteCode());
        roomRepository.save(room);

        return room.getInviteCode();
    }

    public UUID createDMRoom(UserEntity sender, UserEntity receiver) {
        RoomEntity room = new RoomEntity();
        room.setType(RoomTypeEnum.DM);

        RoomEntity roomSaved = roomRepository.save(room);

        roomMemberRepository.save(RoomMemberEntity.builder().id(null).room(roomSaved).user(sender).build());
        roomMemberRepository.save(RoomMemberEntity.builder().id(null).room(roomSaved).user(receiver).build());

        SpaceEntity space = spaceService.createSpace(new CreateSpaceRequest("DM", "CHAT"), roomSaved.getId());

        return space.getId();
    }

    public void deleteRoom(UUID roomId) {
        UUID requesterId = AuthUtils.getCurrentUserId();
        PermissionService.requirePermission(roomId, requesterId, RoomMemberRoleEnum.OWNER);

        RoomEntity room = this.findById(roomId);

    }

    public RoomMemberDto inviteFriendToRoom(UUID roomId, UUID friendId) {
        UUID requesterId = AuthUtils.getCurrentUserId();

        boolean requesterIsMember = roomMemberRepository.existsByRoom_IdAndUser_Id(roomId, requesterId);
        if (!requesterIsMember) {
            throw new RuntimeException("Bạn không phải thành viên của phòng này");
        }

        RoomEntity room = this.findById(roomId);

        RoomMemberEntity existingMember = roomMemberRepository
                .findIncludingInactiveByRoomIdAndUserId(roomId, friendId)
                .orElse(null);
        if (existingMember != null && existingMember.getStatus() == RoomMemberStatusEnum.ACTIVE) {
            throw new RuntimeException("Người này đã ở trong phòng");
        }

        UserEntity friend = userRepository.findById(friendId)
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));

        RoomMemberEntity member = existingMember != null ? existingMember : RoomMemberEntity.builder()
                .room(room).user(friend).build();
        member.setRole(RoomMemberRoleEnum.MEMBER);
        member.setStatus(RoomMemberStatusEnum.ACTIVE);
        member.setJoinedAt(LocalDateTime.now());

        RoomMemberDto dto = new RoomMemberDto(roomMemberRepository.save(member));
        messagingTemplate.convertAndSend("/topic/room/" + room.getId() + "/members/joined", dto);

        return dto;
    }
}
