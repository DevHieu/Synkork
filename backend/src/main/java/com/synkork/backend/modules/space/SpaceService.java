package com.synkork.backend.modules.space;

import com.synkork.backend.modules.room.RoomEntity;
import com.synkork.backend.modules.room.RoomRepository;
import com.synkork.backend.modules.space.dto.SpaceDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class SpaceService {
    @Autowired
    private SpaceRepository spaceRepository;

    @Autowired
    private RoomRepository roomRepository;

    public Optional<SpaceEntity> createSpace(SpaceEntity space, UUID roomId) {
        RoomEntity roomEntity = roomRepository.getReferenceById(roomId);
        space.setRoom(roomEntity);
        return Optional.of(spaceRepository.save(space));
    }

    public List<SpaceDto> getAllSpaceByRoomId(UUID roomId) {
        return spaceRepository.findAllByRoomIdAsDto(roomId);
    }
}
