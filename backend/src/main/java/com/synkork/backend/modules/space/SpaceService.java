package com.synkork.backend.modules.space;

import com.synkork.backend.common.utils.AuthUtils;
import com.synkork.backend.common.utils.PlanLimitUtils;
import com.synkork.backend.modules.collaboration.calendar.repository.CalendarEventRepository;
import com.synkork.backend.modules.collaboration.task.column.ColumnRepository;
import com.synkork.backend.modules.message.MessageRepository;
import com.synkork.backend.modules.room.RoomEntity;
import com.synkork.backend.modules.room.RoomRepository;
import com.synkork.backend.modules.room.dto.CreateRoomDto;
import com.synkork.backend.modules.room.enums.RoomTypeEnum;
import com.synkork.backend.modules.space.dto.CreateSpaceRequest;
import com.synkork.backend.modules.space.dto.SpaceDTO;
import com.synkork.backend.modules.space.dto.UpdateSpaceRequest;
import com.synkork.backend.modules.space.enums.SpaceTypeEnum;
import com.synkork.backend.modules.user.UserEntity;
import com.synkork.backend.modules.user.UserRepository;
import com.synkork.backend.modules.user.enums.PlanEnum;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
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
    private CalendarEventRepository calendarEventRepository;

    @Autowired
    private ColumnRepository columnRepository;

    @Autowired
    private UserRepository userRepository;

//    @Autowired
//    private CalendarEventRepository calendarEventRepository;

    public SpaceEntity createSpace(CreateSpaceRequest space, UUID roomId) {
        RoomEntity roomEntity = roomRepository.getReferenceById(roomId);

        SpaceTypeEnum type = SpaceTypeEnum.valueOf(space.type());

        UUID currentUserId = AuthUtils.getCurrentUserId();
        UserEntity user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));

        PlanEnum plan = user.getCurrentPlan();
        long current = spaceRepository.countByRoom_IdAndType(roomId, type);

        int max = switch (type) {
            case CHAT -> PlanLimitUtils.maxChatSpaces(plan);
            case VOICE -> PlanLimitUtils.maxVoiceSpaces(plan);
            case NOTE, TASK, CALENDAR -> PlanLimitUtils.maxCollaborationSpaces(plan);
            default -> Integer.MAX_VALUE;
        };

        if (current >= max) {
            throw new RuntimeException(
                    "Gói " + plan + " chỉ được tạo tối đa " + max + " " + type + " space. Vui lòng nâng cấp gói."
            );
        }

        SpaceEntity spaceEntity = new SpaceEntity(space.name(), type, roomEntity);
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
            case CHAT -> {
                messageRepository.deleteBySpaceId(spaceId);
            }
            case CALENDAR -> {
                calendarEventRepository.deleteBySpaceId(spaceId);
            }
            case TASK -> {
                columnRepository.deleteBySpaceId(spaceId);
            }
        }
    }

    public SpaceDTO getSpaceById(UUID spaceId) {
        SpaceEntity space = spaceRepository.findById(spaceId)
                .orElseThrow(() -> new IllegalArgumentException("Space not found"));

        return new SpaceDTO(space);
    }

    public Map<String, UUID> createPersonalSpaces(UserEntity user) {
        RoomEntity roomEntity = roomRepository.save(
                RoomEntity.builder().owner(user).type(RoomTypeEnum.DM).build());

        UUID noteId;
        if (user.getPersonalNoteId() == null) {
            noteId = spaceRepository.save(
                    SpaceEntity.builder().room(roomEntity).name("").type(SpaceTypeEnum.NOTE).isRestricted(true).build()).getId();
        } else {
            noteId = user.getPersonalNoteId();
        }

        UUID calendarId;
        if (user.getPersonalCalendarId() == null) {
            calendarId = spaceRepository.save(
                    SpaceEntity.builder().room(roomEntity).name("").type(SpaceTypeEnum.CALENDAR).build()).getId();
        } else {
            calendarId = user.getPersonalCalendarId();
        }

        return Map.of(
                "roomId", roomEntity.getId(),
                "noteId", noteId,
                "calendarId", calendarId);
    }

    public boolean checkUserAccess(UUID spaceId, UUID currentUserId) {
        return spaceRepository.hasAccess(spaceId, currentUserId);
    }
}
