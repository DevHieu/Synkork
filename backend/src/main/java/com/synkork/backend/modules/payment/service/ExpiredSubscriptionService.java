package com.synkork.backend.modules.payment.service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.synkork.backend.common.utils.PlanLimitUtils;
import com.synkork.backend.modules.payment.utils.ExpiredSubscriptionEmail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
import com.synkork.backend.modules.user.enums.PlanEnum;

import jakarta.transaction.Transactional;

@Service
public class ExpiredSubscriptionService {

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private SpaceRepository spaceRepository;

    @Autowired
    private ExpiredSubscriptionEmail expiredSubscriptionEmail;

    @Transactional
    public void pinPendingRemovalRoomAndSpace(List<UserEntity> users) {
        for (UserEntity user : users) {
            PlanEnum effectivePlan = user.getCurrentPlan();
            Map<SpaceTypeEnum, Integer> limits = limitsForPlan(effectivePlan);
            long daysRemaining = user.getPlanExpiresAt() != null
                    ? ChronoUnit.DAYS.between(LocalDateTime.now(), user.getPlanExpiresAt())
                    : 0;

            // reset toàn bộ PENDING_REMOVAL của user này về OPEN
            // để tính lại từ đầu, tránh giữ trạng thái stale từ lần chạy trước
            roomRepository.updatePendingRoomStatusByOwnerId(RoomStatusEnum.OPEN, user.getId());
            spaceRepository.updatePendingSpaceStatusByRoom_OwnerId(SpaceStatusEnum.OPEN, user.getId());

            List<String> pendingRoomNames = List.of();
            Map<String, List<String>> pendingSpaceNames = new LinkedHashMap<>();

            // giờ chỉ cần lấy OPEN, vì PENDING_REMOVAL vừa bị reset hết rồi
            List<RoomEntity> allRooms = roomRepository.findByOwnerIdAndTypeAndStatusInOrderByCreatedAtDesc(
                    user.getId(), RoomTypeEnum.GROUP,
                    List.of(RoomStatusEnum.OPEN)
            );

            List<UUID> ids = allRooms.stream().map(RoomEntity::getId).toList();

            long roomExcess = ids.size() - PlanLimitUtils.maxRooms(effectivePlan);

            if (roomExcess > 0) {
                pendingRoomNames = allRooms.subList(0, (int) roomExcess)
                        .stream().map(RoomEntity::getName).toList();

                List<UUID> pendingIds = ids.subList(0, (int) roomExcess);
                ids = ids.subList((int) roomExcess, ids.size());
                roomRepository.updateStatusByIds(RoomStatusEnum.PENDING_REMOVAL, pendingIds);
            }

            for (int i = 0; i < ids.size(); i++) {
                UUID roomId = ids.get(i);
                String roomName = allRooms.get((int) (roomExcess > 0 ? roomExcess : 0) + i).getName();
                List<String> spacesToDelete = new ArrayList<>();

                for (Map.Entry<SpaceTypeEnum, Integer> entry : limits.entrySet()) {
                    SpaceTypeEnum type = entry.getKey();
                    int limit = entry.getValue();

                    // cũng chỉ cần OPEN vì đã reset PENDING_REMOVAL
                    List<SpaceEntity> spaces = spaceRepository.findByRoomIdAndTypeAndStatusInOrderByCreatedAtDesc(
                            roomId, type, List.of(SpaceStatusEnum.OPEN));
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

            expiredSubscriptionEmail.sendRemindUserRenewSubscription(
                    user.getEmail(), effectivePlan, daysRemaining,
                    pendingRoomNames, pendingSpaceNames
            );
        }
    }

    private Map<SpaceTypeEnum, Integer> limitsForPlan(PlanEnum plan) {
        return Map.of(
                SpaceTypeEnum.CHAT, PlanLimitUtils.maxChatSpaces(plan),
                SpaceTypeEnum.VOICE, PlanLimitUtils.maxVoiceSpaces(plan),
                SpaceTypeEnum.CALENDAR, PlanLimitUtils.maxCollaborationSpaces(plan),
                SpaceTypeEnum.NOTE, PlanLimitUtils.maxCollaborationSpaces(plan),
                SpaceTypeEnum.TASK, PlanLimitUtils.maxCollaborationSpaces(plan)
        );
    }

    @Transactional
    public void changePendingRoomAndSpace(UUID userId) {
        roomRepository.updatePendingRoomStatusByOwnerId(RoomStatusEnum.OPEN, userId);
        spaceRepository.updatePendingSpaceStatusByRoom_OwnerId(SpaceStatusEnum.OPEN, userId);
    }
}