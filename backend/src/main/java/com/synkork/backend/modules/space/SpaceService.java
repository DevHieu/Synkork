package com.synkork.backend.modules.space;

import com.synkork.backend.common.utils.AuthUtils;
import com.synkork.backend.common.utils.PlanLimitUtils;
import com.synkork.backend.modules.collaboration.calendar.repository.CalendarEventRepository;
import com.synkork.backend.modules.collaboration.task.column.ColumnRepository;
import com.synkork.backend.modules.message.MessageRepository;
import com.synkork.backend.modules.room.RoomEntity;
import com.synkork.backend.modules.room.RoomRepository;
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

        SpaceEntity spaceEntity = SpaceEntity.builder()
                .name(space.name())
                .type(type)
                .room(roomEntity)
                .build();
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

        // Personal Calendar cần tồn tại lâu dài nên không cho phép xóa Personal Space.
        if (space.getRoom().getType() == RoomTypeEnum.PERSONAL) {
            throw new IllegalArgumentException("Không thể xóa space cá nhân");
        }

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

    // Tìm/tạo Personal Room và Calendar trong một transaction để không lưu trạng thái dở dang.
    @Transactional
    public Map<String, UUID> createPersonalSpaces(UserEntity user) {
        SpaceEntity note = findPersonalSpace(user.getPersonalNoteId(), SpaceTypeEnum.NOTE);
        SpaceEntity calendar = findPersonalSpace(user.getPersonalCalendarId(), SpaceTypeEnum.CALENDAR);

        RoomEntity roomEntity = note != null ? note.getRoom() : calendar != null ? calendar.getRoom() : null;
        if (roomEntity == null || roomEntity.getType() != RoomTypeEnum.PERSONAL) {
            roomEntity = roomRepository.findFirstByOwnerIdAndType(user.getId(), RoomTypeEnum.PERSONAL)
                    .orElseGet(() -> roomRepository.save(RoomEntity.builder().owner(user).type(RoomTypeEnum.PERSONAL).build()));
        }

        if (note == null || !note.getRoom().getId().equals(roomEntity.getId())) {
            note = spaceRepository.findFirstByRoom_IdAndTypeOrderByCreatedAtAsc(roomEntity.getId(), SpaceTypeEnum.NOTE)
                    .orElse(null);
        }
        if (note == null) {
            note = spaceRepository.save(SpaceEntity.builder().room(roomEntity).name("")
                    .type(SpaceTypeEnum.NOTE).isRestricted(true).build());
        }

        if (calendar == null || !calendar.getRoom().getId().equals(roomEntity.getId())) {
            calendar = spaceRepository.findFirstByRoom_IdAndTypeOrderByCreatedAtAsc(roomEntity.getId(), SpaceTypeEnum.CALENDAR)
                    .orElse(null);
        }
        if (calendar == null) {
            calendar = spaceRepository.save(SpaceEntity.builder().room(roomEntity).name("")
                    .type(SpaceTypeEnum.CALENDAR).build());
        }

        // Lưu lại ID Calendar để các lần đăng nhập sau mở đúng Personal Calendar đã có.
        user.setPersonalNoteId(note.getId());
        user.setPersonalCalendarId(calendar.getId());
        userRepository.save(user);
        return Map.of("roomId", roomEntity.getId(), "noteId", note.getId(), "calendarId", calendar.getId());
    }

    // Chỉ dùng ID nếu nó trỏ đúng loại Space; tránh lấy nhầm Space khi khôi phục Personal Calendar.
    private SpaceEntity findPersonalSpace(UUID spaceId, SpaceTypeEnum type) {
        if (spaceId == null) return null;
        return spaceRepository.findById(spaceId)
                .filter(space -> space.getType() == type)
                .orElse(null);
    }

    public boolean checkUserAccess(UUID spaceId, UUID currentUserId) {
        return spaceRepository.hasAccess(spaceId, currentUserId);
    }
}
