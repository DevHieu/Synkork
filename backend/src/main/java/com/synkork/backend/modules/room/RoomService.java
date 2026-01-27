package com.synkork.backend.modules.room;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import com.synkork.backend.modules.room.dto.RoomDto;
import com.synkork.backend.modules.user.UserRepository;

@Service
public class RoomService {

  @Autowired
  RoomRepository roomRepository;

  @Autowired
  UserRepository userRepository;

  public Optional<RoomEntity> createSpace(@NonNull RoomDto room) {
    RoomEntity roomEntity = new RoomEntity();

    roomEntity.setName(room.name());
    roomEntity.setRoomAvatar(room.roomAvatar());

    UUID ownerId = room.ownerId();
    if (ownerId != null) {
      roomEntity.setOwner(userRepository.getReferenceById(ownerId));
    }

    return Optional.of(roomRepository.save(roomEntity));
  }

  public List<RoomEntity> findRoomUserJoined(@NonNull UUID userId) {
    return roomRepository.findRoomMembersJoined(userId);
  }
}
