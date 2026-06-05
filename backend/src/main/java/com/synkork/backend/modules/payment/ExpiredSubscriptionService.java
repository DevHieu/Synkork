package com.synkork.backend.modules.payment;

import com.synkork.backend.common.utils.EmailService;
import com.synkork.backend.modules.room.RoomEntity;
import com.synkork.backend.modules.room.RoomRepository;
import com.synkork.backend.modules.room.enums.RoomStatusEnum;
import com.synkork.backend.modules.room.enums.RoomTypeEnum;
import com.synkork.backend.modules.space.SpaceEntity;
import com.synkork.backend.modules.space.SpaceRepository;
import com.synkork.backend.modules.space.enums.SpaceStatusEnum;
import com.synkork.backend.modules.space.enums.SpaceTypeEnum;
import com.synkork.backend.modules.user.UserEntity;
import com.synkork.backend.modules.user.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
public class ExpiredSubscriptionService {

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private SpaceRepository spaceRepository;

    Map<SpaceTypeEnum, Integer> freeLimits = Map.of(
            SpaceTypeEnum.CHAT, 3,
            SpaceTypeEnum.VOICE, 2,
            SpaceTypeEnum.CALENDAR, 1,
            SpaceTypeEnum.NOTE, 1,
            SpaceTypeEnum.TASK, 1
    );

    @Transactional
    public void pinPendingRemovalRoomAndSpace(List<UserEntity> users) {
        for (UserEntity user : users) {
            long daysRemaining = ChronoUnit.DAYS.between(LocalDateTime.now(), user.getPlanExpiresAt());

            List<String> pendingRoomNames = List.of(); // rooms sẽ bị xóa
            Map<String, List<String>> pendingSpaceNames = new LinkedHashMap<>(); // room name -> space names sẽ bị xóa

            List<RoomEntity> allRooms = roomRepository.findByOwnerIdAndTypeAndStatusInOrderByCreatedAtDesc(
                    user.getId(), RoomTypeEnum.GROUP,
                    List.of(RoomStatusEnum.OPEN, RoomStatusEnum.PENDING_REMOVAL)
            );

            List<UUID> ids = allRooms.stream().map(RoomEntity::getId).toList();

            // Tính số lượng dư
            long roomExcess = ids.size() - 5;

            if (roomExcess > 0) {
                // Chỉnh status của các room tạo gần đây nhất
                pendingRoomNames = allRooms.subList(0, (int) roomExcess)
                        .stream().map(RoomEntity::getName).toList();

                List<UUID> pendingIds = ids.subList(0, (int) roomExcess);
                ids = ids.subList((int) roomExcess, ids.size());
                roomRepository.updateStatusByIds(RoomStatusEnum.PENDING_REMOVAL, pendingIds);
            }

            // Space bị xóa trong room giữ lại
            for (int i = 0; i < ids.size(); i++) {
                UUID roomId = ids.get(i);
                String roomName = allRooms.get((int) (roomExcess > 0 ? roomExcess : 0) + i).getName();
                List<String> spacesToDelete = new ArrayList<>();

                for (Map.Entry<SpaceTypeEnum, Integer> entry : freeLimits.entrySet()) {
                    SpaceTypeEnum type = entry.getKey();
                    int limit = entry.getValue();

                    List<SpaceEntity> spaces = spaceRepository.findByRoomIdAndTypeOrderByCreatedAtDesc(roomId, type);
                    List<UUID> spaceIds = spaces.stream().map(SpaceEntity::getId).toList();
                    long spaceExcess = spaceIds.size() - limit;

                    if (spaceExcess > 0) {
                        spaces.subList(0, (int) spaceExcess)
                                .forEach(s -> spacesToDelete.add("[" + type + "] " + s.getName()));

                        List<UUID> pendingSpaceIds = spaceIds.subList(0, (int) spaceExcess);
                        spaceRepository.updateStatusByIds(SpaceStatusEnum.PENDING_REMOVAL, pendingSpaceIds);
                    }
                }

                if (!spacesToDelete.isEmpty()) {
                    pendingSpaceNames.put(roomName, spacesToDelete);
                }
            }

            emailService.sendRemindUserRenewSubscription(
                    user.getEmail(), user.getCurrentPlan(), daysRemaining,
                    pendingRoomNames, pendingSpaceNames
            );
        }
    }

    @Transactional
    public void changePendingRoomAndSpace(UUID userId) {
        roomRepository.updatePendingRoomStatusByOwnerId(RoomStatusEnum.OPEN, userId);
        spaceRepository.updatePendingSpaceStatusByRoom_OwnerId(SpaceStatusEnum.OPEN, userId);
    }
}
