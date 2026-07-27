package com.synkork.backend.modules.roomMember;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.synkork.backend.modules.roomMember.enums.MemberStatusEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.synkork.backend.common.utils.PermissionService;
import com.synkork.backend.modules.room.RoomEntity;
import com.synkork.backend.modules.room.RoomRepository;
import com.synkork.backend.modules.roomMember.dto.ChangeAuthorityDTO;
import com.synkork.backend.modules.roomMember.dto.ChatDisableRequest;
import com.synkork.backend.modules.roomMember.dto.MuteRequest;
import com.synkork.backend.modules.roomMember.dto.RoomMemberDto;
import com.synkork.backend.modules.roomMember.enums.ChatDisableTime;
import com.synkork.backend.modules.roomMember.enums.RoomMemberRoleEnum;
import com.synkork.backend.modules.user.UserEntity;
import com.synkork.backend.modules.user.UserRepository;

@Service
public class RoomMemberService {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    RoomRepository roomRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    RoomMemberRepository roomMemberRepository;

    public RoomMemberEntity getRoomMemberById(UUID memberId) {
        return roomMemberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Member not found"));
    }

    public RoomMemberEntity getRoomMemberByRoomIdAndUserId(UUID roomId, UUID userId) {
        return roomMemberRepository
                .findByRoom_IdAndUser_Id(roomId, userId)
                .orElseThrow(() -> new RuntimeException("Member not found"));
    }

    public RoomMemberEntity getRoomMemberByRoomIdAndMemberId(UUID roomId, UUID memberId) {
        return roomMemberRepository
                .findByRoom_IdAndId(roomId, memberId)
                .orElseThrow(() -> new RuntimeException("Member not found"));
    }

    public List<RoomMemberDto> getRoomMembers(String roomId) {
        return roomMemberRepository.findByRoom_IdAndStatus(UUID.fromString(roomId), MemberStatusEnum.ACTIVE)
                .stream()
                .map(RoomMemberDto::new)
                .toList();
    }

    public RoomMemberEntity addRoomMembers(String userId, String roomID, String role) {
        RoomMemberEntity roomMemberEntity = new RoomMemberEntity();

        RoomEntity room = roomRepository.findById(UUID.fromString(roomID))
                .orElseThrow(() -> new RuntimeException("Room not found: " + roomID));

        UserEntity user = userRepository.findById(UUID.fromString(userId))
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        roomMemberEntity.setRoom(room);
        roomMemberEntity.setUser(user);

        try {
            roomMemberEntity.setRole(role != null
                    ? RoomMemberRoleEnum.valueOf(role.toUpperCase())
                    : RoomMemberRoleEnum.MEMBER);
        } catch (IllegalArgumentException e) {
            roomMemberEntity.setRole(RoomMemberRoleEnum.MEMBER);
        }

        return roomMemberRepository.save(roomMemberEntity);
    }

    @Transactional
    public List<RoomMemberEntity> changerAuthority(ChangeAuthorityDTO dto, UUID roomId, UUID requesterUserId) {

        PermissionService.requirePermission(roomId, requesterUserId, RoomMemberRoleEnum.OWNER, RoomMemberRoleEnum.ADMIN);

        UUID memberUUID = UUID.fromString(dto.memberId());
        RoomMemberEntity member = this.getRoomMemberByRoomIdAndMemberId(roomId, memberUUID);

        if (member.getRole() == RoomMemberRoleEnum.OWNER) {
            throw new RuntimeException("Không thể đổi quyền chủ phòng");
        }

        RoomMemberRoleEnum newRole = RoomMemberRoleEnum.valueOf(dto.newRole());

        if (newRole == RoomMemberRoleEnum.OWNER) {
            RoomMemberEntity currentOwner = this.getRoomMemberByRoomIdAndUserId(roomId, requesterUserId);
            currentOwner.setRole(RoomMemberRoleEnum.ADMIN);
            member.setRole(RoomMemberRoleEnum.OWNER);

            RoomEntity room = member.getRoom();
            room.setOwner(member.getUser());

            roomMemberRepository.save(currentOwner);
            RoomMemberEntity newOwner = roomMemberRepository.save(member);
            roomRepository.save(room);

            return List.of(currentOwner, newOwner);
        }

        member.setRole(newRole);
        return List.of(roomMemberRepository.save(member));
    }

    @Transactional
    public String kickMember(UUID memberUUID, UUID roomUUID, UUID userId) {
        RoomMemberEntity kicker = this.getRoomMemberByRoomIdAndUserId(roomUUID, userId);
        PermissionService.requirePermission(kicker, RoomMemberRoleEnum.OWNER, RoomMemberRoleEnum.ADMIN);

        RoomMemberEntity target = this.getRoomMemberById(memberUUID);;

        if (target.getRole() == RoomMemberRoleEnum.OWNER) {
            throw new RuntimeException("Cannot kick OWNER");
        }

        if (kicker.getRole() == RoomMemberRoleEnum.ADMIN
                && target.getRole() == RoomMemberRoleEnum.ADMIN) {
            throw new RuntimeException("ADMIN cannot kick another ADMIN");
        }

        target.setStatus(MemberStatusEnum.KICKED);
        roomMemberRepository.save(target);

        return target.getUser().getEmail();
    }

    public RoomMemberEntity setChatMuteMember(UUID memberUUID, UUID roomUUID, UUID requesterId, ChatDisableTime chatDisableTime) {

        PermissionService.requirePermission(roomUUID, requesterId, RoomMemberRoleEnum.OWNER, RoomMemberRoleEnum.ADMIN);

        LocalDateTime now = LocalDateTime.now();

        LocalDateTime chatMutedUntil = switch (chatDisableTime) {
            case NOT_DISABLE -> null;
            case MINUTE -> now.plusMinutes(1);
            case FIVE_MINUTES -> now.plusMinutes(5);
            case FIFTEEN_MINUTES -> now.plusMinutes(15);
            case HOUR -> now.plusHours(1);
            case DAY -> now.plusDays(1);
            case WEEK -> now.plusWeeks(1);
        };

        RoomMemberEntity target = this.getRoomMemberByRoomIdAndMemberId(roomUUID, memberUUID);

        target.setChatDisableUntil(chatMutedUntil);

        return roomMemberRepository.save(target);
    }

    public RoomMemberEntity toggleMuteMembers(UUID roomId, UUID memberId, UUID requesterId, MuteRequest muteRequest) {


        PermissionService.requirePermission(roomId, requesterId, RoomMemberRoleEnum.OWNER, RoomMemberRoleEnum.ADMIN);

        RoomMemberEntity member = this.getRoomMemberByMemberIdOrUserId(roomId, memberId);

        if (muteRequest.muted() != null) {
            member.setMuted(muteRequest.muted());
        }

        if (muteRequest.deafen() != null) {
            member.setDeafen(muteRequest.deafen());
        }

        return roomMemberRepository.save(member);
    }

    public RoomMemberEntity changeChatDisable(UUID roomId, UUID memberId, UUID requesterId, ChatDisableRequest request) {
        PermissionService.requirePermission(roomId, requesterId, RoomMemberRoleEnum.OWNER, RoomMemberRoleEnum.ADMIN);

        RoomMemberEntity member = this.getRoomMemberByMemberIdOrUserId(roomId, memberId);
        ChatDisableTime time = request == null || request.time() == null
                ? ChatDisableTime.NOT_DISABLE
                : request.time();

        member.setChatDisableUntil(calculateChatDisableUntil(time));

        return roomMemberRepository.save(member);
    }

    private RoomMemberEntity getRoomMemberByMemberIdOrUserId(UUID roomId, UUID memberIdOrUserId) {
        return roomMemberRepository.findByIdAndRoom_Id(memberIdOrUserId, roomId)
                .or(() -> roomMemberRepository.findByRoom_IdAndUser_Id(roomId, memberIdOrUserId))
                .orElseThrow(() -> new RuntimeException("Member not found"));
    }

    private LocalDateTime calculateChatDisableUntil(ChatDisableTime time) {
        LocalDateTime now = LocalDateTime.now();

        return switch (time) {
            case MINUTE -> now.plusMinutes(1);
            case FIVE_MINUTES -> now.plusMinutes(5);
            case FIFTEEN_MINUTES -> now.plusMinutes(15);
            case HOUR -> now.plusHours(1);
            case DAY -> now.plusDays(1);
            case WEEK -> now.plusWeeks(1);
            case NOT_DISABLE -> null;
        };
    }

    public List<UserEntity> getRoomMemberByRoomId(UUID roomUUID) {
        return roomMemberRepository.findUsersByRoomId(roomUUID);
    }

    @Transactional
    public void leaveRoom(UUID roomUUID, UUID requesterId) {
        RoomMemberEntity member = this.getRoomMemberByRoomIdAndUserId(roomUUID, requesterId);
        member.setStatus(MemberStatusEnum.KICKED);
        roomMemberRepository.save(member);

//        roomMemberRepository.removeFromCardAssignees(member.getId());
//        roomMemberRepository.removeFromCalendarEventRoomMembers(member.getId());
//        roomMemberRepository.delete(member);
    }

    public void deleteMember(UUID userId, UUID roomId) {
        RoomMemberEntity member = this.getRoomMemberByRoomIdAndUserId(roomId, userId);
        member.setStatus(MemberStatusEnum.KICKED);
        roomMemberRepository.save(member);
    }

    public void transferOwnerBeforeRemoving(RoomEntity room, List<RoomMemberEntity> remainingMembers) {
        Optional<RoomMemberEntity> newOwner = remainingMembers.stream()
                .filter(member -> member.getRole() == RoomMemberRoleEnum.ADMIN)
                .min(joinedAtComparator());

        if (newOwner.isEmpty()) {
            newOwner = remainingMembers.stream()
                    .min(joinedAtComparator());
        }

        if (newOwner.isEmpty()) {
            room.setOwner(null);
            roomRepository.save(room);
            return;
        }

        RoomMemberEntity ownerMember = newOwner.get();
        ownerMember.setRole(RoomMemberRoleEnum.OWNER);
        ownerMember.setStatus(MemberStatusEnum.ACTIVE);
        room.setOwner(ownerMember.getUser());
        roomMemberRepository.save(ownerMember);
        roomRepository.save(room);

        System.out.println("\"/topic/room/\" + room.getId() + \"/members/changeAuthority\"");
        System.out.println(ownerMember.getUser().getUsername());
        messagingTemplate.convertAndSend(
                "/topic/room/" + room.getId() + "/members/changeAuthority", new RoomMemberDto(ownerMember)
        );
    }

    private Comparator<RoomMemberEntity> joinedAtComparator() {
        return Comparator.comparing(
                RoomMemberEntity::getJoinedAt,
                Comparator.nullsLast(Comparator.naturalOrder())
        );
    }
}
