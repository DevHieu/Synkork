package com.synkork.backend.modules.room;

import com.synkork.backend.common.dtos.FileUploaded;
import com.synkork.backend.common.utils.FileService;
import com.synkork.backend.modules.room.dto.CreateRoomDto;
import com.synkork.backend.modules.room.dto.RoomDto;
import com.synkork.backend.modules.room.dto.RoomReviewResponse;
import com.synkork.backend.modules.room.enums.RoomTypeEnum;
import com.synkork.backend.modules.roomMember.RoomMemberEntity;
import com.synkork.backend.modules.roomMember.RoomMemberRepository;
import com.synkork.backend.modules.roomMember.dto.RoomMemberDto;
import com.synkork.backend.modules.roomMember.enums.RoomMemberRoleEnum;
import com.synkork.backend.modules.space.SpaceEntity;
import com.synkork.backend.modules.space.SpaceService;
import com.synkork.backend.modules.space.dto.CreateSpaceRequest;
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

    public Optional<RoomEntity> createRoom(CreateRoomDto roomData) {
        RoomEntity roomEntity = new RoomEntity();

        roomEntity.setName(roomData.name());
        roomEntity.setInviteCode(generateInviteCode());

        if (roomData.imageFile() != null) {
            FileUploaded avatar = imageService.uploadImage(roomData.imageFile(), "roomAvatar");

            roomEntity.setAvatarUrl(avatar.url());
            roomEntity.setAvatarId(avatar.publicId());
        }

        UUID ownerId = null;
        if (roomData.ownerId() != null) {
            ownerId = UUID.fromString(roomData.ownerId());
            roomEntity.setOwner(userRepository.getReferenceById(ownerId));
        }

        if (ownerId != null) {
            roomEntity.setOwner(userRepository.getReferenceById(ownerId));
        }

        return Optional.of(roomRepository.save(roomEntity));
    }

    public List<RoomEntity> findRoomUserJoined(@NonNull UUID userId) {
        return roomRepository.findRoomMembersJoined(userId);
    }

    public RoomReviewResponse getRoomByInviteCode(String code) {
        RoomEntity room = roomRepository.findByInviteCode(code).orElseThrow(() -> new RuntimeException("Link mời không tồn tại"));

        return RoomReviewResponse.builder()
                .roomName(room.getName())
                .roomAvatar(room.getAvatarUrl())
                .roomMembers(roomMemberRepository.countByRoom_Id(room.getId()))
                .build();
    }

    // Return Room để tao làm khi join phòng xong sẽ tự vào room vừa gia nhập
    public RoomDto joinRoom(String code, UUID userId) {
        RoomEntity room = roomRepository.findByInviteCode(code).orElseThrow(() -> new RuntimeException("Link mời không tồn tại"));

        boolean alreadyMember = roomMemberRepository.existsByRoom_IdAndUser_Id(room.getId(), userId);
        if (alreadyMember) throw new RuntimeException("Bạn đã là thành viên của phòng này");

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));

        RoomMemberEntity member = RoomMemberEntity.builder()
                .room(room)
                .user(user)
                .role(RoomMemberRoleEnum.MEMBER)
                .build();

        // Vừa save vừa convert sang dto luôn
        RoomMemberDto dto = new  RoomMemberDto(roomMemberRepository.save(member));

        messagingTemplate.convertAndSend("/topic/room/" + room.getId() + "/members/joined", dto);

        return new RoomDto(
                room.getId(),
                room.getName(),
                room.getDescription(),
                room.getAvatarUrl()
        );
    }

    // Reset invite code
    public String resetInviteCode(String roomId) {
        RoomEntity room = roomRepository.findById(UUID.fromString(roomId))
                .orElseThrow(() -> new RuntimeException("Room không tồn tại"));

        room.setInviteCode(generateInviteCode());
        roomRepository.save(room);

        return room.getInviteCode();
    }

    public UUID createDMRoom(UserEntity sender, UserEntity receiver) {
        RoomEntity room = new RoomEntity();
        room.setType(RoomTypeEnum.DM);
        RoomEntity roomSaved = roomRepository.save(room);

        roomMemberRepository.save(RoomMemberEntity.builder().id(null).room(room).user(sender).build());
        roomMemberRepository.save(RoomMemberEntity.builder().id(null).room(room).user(receiver).build());

        SpaceEntity space = spaceService.createSpace(new CreateSpaceRequest("DM", "CHAT"), roomSaved.getId());

        return space.getId();
    }
}
