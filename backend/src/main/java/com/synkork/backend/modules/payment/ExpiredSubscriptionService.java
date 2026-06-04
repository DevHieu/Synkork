package com.synkork.backend.modules.payment;

import com.synkork.backend.common.utils.EmailService;
import com.synkork.backend.modules.room.RoomEntity;
import com.synkork.backend.modules.room.RoomRepository;
import com.synkork.backend.modules.room.enums.RoomStatusEnum;
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

            // Chỉ cần sửa khi remaining còn 2 ngày. Tại remaining còn 1 hoặc 0 ngày thì cảm giác gọi nhiều quá, sợ yếu hiệu năng lém
            if (daysRemaining == 2) {
                List<RoomEntity> allRooms = roomRepository.findByOwnerIdOrderByCreatedAtDesc(user.getId());
                List<UUID> ids = allRooms.stream().map(RoomEntity::getId).toList();

                // Tính số lượng dư
                long excess = ids.size() - 5;

                if (excess > 0) {
                    // Chỉnh status của các room tạo gần đây nhất
                    pendingRoomNames = allRooms.subList(0, (int) excess)
                            .stream().map(RoomEntity::getName).toList();

                    List<UUID> pendingIds = ids.subList(0, (int) excess);
                    ids = ids.subList((int) excess, ids.size());
                    roomRepository.updateStatusByIds(RoomStatusEnum.PENDING_REMOVAL, pendingIds);
                }

                // Space bị xóa trong room giữ lại
                for (int i = 0; i < ids.size(); i++) {
                    UUID roomId = ids.get(i);
                    String roomName = allRooms.get((int) (excess > 0 ? excess : 0) + i).getName();
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
            }

            emailService.sendRemindUserRenewSubscription(
                    user.getEmail(), user.getCurrentPlan(), daysRemaining,
                    pendingRoomNames, pendingSpaceNames
            );
        }
    }
}
