package com.synkork.backend.modules.space;

import com.synkork.backend.modules.collaboration.calendar.CalendarEventRepository;
import com.synkork.backend.modules.collaboration.task.column.ColumnRepository;
import com.synkork.backend.modules.message.MessageRepository;
import com.synkork.backend.modules.room.RoomEntity;
import com.synkork.backend.modules.room.RoomRepository;
import com.synkork.backend.modules.space.dto.SpaceDTO;
import com.synkork.backend.modules.space.dto.CreateSpaceRequest;
import com.synkork.backend.modules.space.dto.UpdateSpaceRequest;
import com.synkork.backend.modules.space.enums.SpaceTypeEnum;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class SpaceService {
    @Autowired
    private SpaceRepository spaceRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private CalendarEventRepository  calendarEventRepository;

    @Autowired
    private ColumnRepository columnRepository;

//    @Autowired
//    private CalendarEventRepository calendarEventRepository;

    public SpaceEntity createSpace(CreateSpaceRequest space, UUID roomId) {
        RoomEntity roomEntity = roomRepository.getReferenceById(roomId);

        SpaceEntity spaceEntity = new SpaceEntity(space.name(), SpaceTypeEnum.valueOf(space.type()), roomEntity);

        return spaceRepository.save(spaceEntity);
    }

    public List<SpaceDTO> getAllSpaceByRoomId(UUID roomId) {
        return spaceRepository.findAllByRoomIdAsDto(roomId);
    }

    public SpaceEntity updateSpace(UpdateSpaceRequest spaceDto, UUID spaceId) {
        SpaceEntity space = spaceRepository.findById(spaceId)
                .orElseThrow(() -> new IllegalArgumentException("Space not found"));

        space.setName(spaceDto.name());
        space.setRestricted(spaceDto.restricted());

        return spaceRepository.save(space);
    }

    @Transactional
    public void deleteSpace(UUID spaceId) {
        SpaceEntity space = spaceRepository.findById(spaceId)
                .orElseThrow(() -> new IllegalArgumentException("Space not found"));

        this.deleteItem(space.getId(), space.getType());

        spaceRepository.delete(space);
    }

    public void deleteItem(UUID spaceId, SpaceTypeEnum type) {
        switch (type) {
            case CHAT ->  {
                messageRepository.deleteBySpaceId(spaceId);
            }
            case CALENDAR ->   {
                calendarEventRepository.deleteBySpaceId(spaceId);
            }
            case TASK -> {
                columnRepository.deleteBySpaceId(spaceId);
            }
        }
    }

    public SpaceDTO getSpaceById(UUID spaceId) {
        SpaceEntity space =  spaceRepository.findById(spaceId)
                .orElseThrow(() -> new IllegalArgumentException("Space not found"));

        return new SpaceDTO(space);
    }
}
