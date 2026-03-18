package com.synkork.backend.modules.room;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.synkork.backend.common.dtos.ImageCreated;
import com.synkork.backend.common.utils.ImageService;
import com.synkork.backend.modules.room.dto.CreateRoomDto;
import com.synkork.backend.modules.user.UserEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import com.synkork.backend.modules.user.UserRepository;

@Service
public class RoomService {

  @Autowired
  RoomRepository roomRepository;

  @Autowired
  UserRepository userRepository;

  @Autowired
  RoomMemberRepository roomMemberRepository;

  @Autowired
  ImageService imageService;

  public Optional<RoomEntity> createRoom(CreateRoomDto roomData) {
    RoomEntity roomEntity = new RoomEntity();

    roomEntity.setName(roomData.name());

    if (roomData.imageFile() != null) {
        ImageCreated avatar = imageService.uploadImage(roomData.imageFile(), "roomAvatar");

        roomEntity.setAvatarUrl(avatar.imageUrl());
        roomEntity.setAvatarId(avatar.imagePublicId());
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
}
