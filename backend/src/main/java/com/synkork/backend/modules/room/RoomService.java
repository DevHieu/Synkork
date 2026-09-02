package com.synkork.backend.modules.room;

import com.synkork.backend.common.dtos.FileUploaded;
import com.synkork.backend.common.utils.AuthUtils;
import com.synkork.backend.common.utils.FileService;
import com.synkork.backend.common.utils.PermissionService;
import com.synkork.backend.modules.notification.NotificationService;
import com.synkork.backend.modules.notification.enums.NotificationRefTypeEnum;
import com.synkork.backend.modules.notification.enums.NotificationTypeEnum;
import com.synkork.backend.modules.room.dto.CreateRoomRequest;
import com.synkork.backend.modules.room.dto.RoomDto;
import com.synkork.backend.modules.room.dto.RoomReviewResponse;
import com.synkork.backend.modules.room.dto.UpdateRoomRequest;
import com.synkork.backend.modules.room.enums.RoomStatusEnum;
import com.synkork.backend.modules.room.enums.RoomTypeEnum;
import com.synkork.backend.modules.roomMember.RoomMemberEntity;
import com.synkork.backend.modules.roomMember.RoomMemberRepository;
import com.synkork.backend.modules.roomMember.dto.RoomMemberDto;
import com.synkork.backend.modules.roomMember.enums.MemberStatusEnum;
import com.synkork.backend.modules.roomMember.enums.RoomMemberRoleEnum;
import com.synkork.backend.modules.space.SpaceEntity;
import com.synkork.backend.modules.space.SpaceService;
import com.synkork.backend.modules.space.dto.CreateSpaceRequest;
import com.synkork.backend.common.utils.PlanLimitUtils;
import com.synkork.backend.modules.user.UserEntity;
import com.synkork.backend.modules.user.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class RoomService {

    @Autowired
    RoomRepository roomRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    RoomMemberRepository roomMemberRepository;

    @Autowired
    FileService imageService;

    @Autowired
    SimpMessagingTemplate messagingTemplate;

    @Autowired
    private SpaceService spaceService;
    @Autowired
    private NotificationService notificationService;

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

    public UserEntity findOwnerByRoomId(UUID roomId) {
        return roomRepository.findOwnerByRoomId(roomId).orElseThrow(() -> new RuntimeException("Room owner not found!"));
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

    public RoomEntity createRoom(CreateRoomRequest roomData, UUID creatorId) {

        if (creatorId != null) {
            UserEntity owner = userRepository.findById(creatorId)
                    .orElseThrow(() -> new RuntimeException("User không tồn tại"));

            // Check xem owner được chọn có quá số lượng phòng theo gói hay không
            if (!PlanLimitUtils.checkMaxRooms(owner.getCurrentPlan(), owner.getId())) {
                return null;
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

        if (creatorId != null) {
            roomEntity.setOwner(userRepository.getReferenceById(creatorId));
        }

        return roomRepository.save(roomEntity);
    }

    public RoomEntity updateRoom(UUID roomId, UpdateRoomRequest roomData) {

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

    public RoomEntity joinRoom(String code, UUID userId) {
        RoomEntity room = roomRepository.findByInviteCode(code)
                .orElseThrow(() -> new RuntimeException("Link mời không tồn tại"));

        Optional<RoomMemberEntity> alreadyMember = roomMemberRepository.findByRoom_IdAndUser_Id(room.getId(), userId);

        // Check xem mmber đã trong phòng? Đã trong phòng thì statsu là gì
        if (alreadyMember.isPresent()) {
            RoomMemberEntity member = alreadyMember.get();
            if (member.getStatus() == MemberStatusEnum.KICKED) {
                member.setStatus(MemberStatusEnum.ACTIVE);
                roomMemberRepository.save(member);

                return room;
            } else {
                throw new RuntimeException("Bạn đã là thành viên của phòng này");
            }
        }

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));

        RoomMemberEntity member = RoomMemberEntity.builder()
                .room(room)
                .user(user)
                .role(RoomMemberRoleEnum.MEMBER)
                .build();

        // Vừa save vừa convert sang dto luôn
        RoomMemberDto dto = new RoomMemberDto(roomMemberRepository.save(member));

        messagingTemplate.convertAndSend("/topic/room/" + room.getId() + "/members/joined", dto);

        return room;
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
        room.setStatus(RoomStatusEnum.LOCKED);
    }

    public RoomMemberDto inviteFriendToRoom(UUID roomId, UUID friendId) {
        UUID requesterId = AuthUtils.getCurrentUserId();

        boolean requesterIsMember = roomMemberRepository.existsByRoom_IdAndUser_Id(roomId, requesterId);
        if (!requesterIsMember) {
            throw new RuntimeException("Bạn không phải thành viên của phòng này");
        }

        RoomEntity room = this.findById(roomId);

        Optional<RoomMemberEntity> alreadyMember = roomMemberRepository.findByRoom_IdAndUser_Id(room.getId(), friendId);

        UserEntity friend = userRepository.findById(friendId)
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));

        // Check xem mmber đã trong phòng? Đã trong phòng thì statsu là gì
        if (alreadyMember.isPresent()) {
            RoomMemberEntity member = alreadyMember.get();
            System.out.println(member.getStatus());
            if (member.getStatus() == MemberStatusEnum.KICKED) {
                member.setStatus(MemberStatusEnum.ACTIVE);
                roomMemberRepository.save(member);

                RoomMemberDto dto = new RoomMemberDto(member);
                messagingTemplate.convertAndSend("/topic/room/" + room.getId() + "/members/joined", dto);

                messagingTemplate.convertAndSendToUser(member.getUser().getEmail(), "/queue/room/members/invited", "Đã thêm mới vào phòng");
                notificationService.sendNotification(null, friend, null, roomId, null, NotificationTypeEnum.MEMBER, NotificationRefTypeEnum.MEMBER_INVITED);

                return dto;
            } else {
                throw new RuntimeException("Người này đã ở trong phòng");
            }
        }

        RoomMemberEntity member = RoomMemberEntity.builder()
                .room(room)
                .user(friend)
                .role(RoomMemberRoleEnum.MEMBER)
                .build();

        RoomMemberDto dto = new RoomMemberDto(roomMemberRepository.save(member));

        messagingTemplate.convertAndSend("/topic/room/" + room.getId() + "/members/joined", dto);
        notificationService.sendNotification(null, friend, null, roomId, null, NotificationTypeEnum.MEMBER, NotificationRefTypeEnum.MEMBER_INVITED);

        return dto;
    }
}
