package com.synkork.backend.modules.payment.utils;

import com.google.common.collect.Lists;
import com.synkork.backend.common.utils.EmailService;
import com.synkork.backend.modules.payment.service.ExpiredSubscriptionService;
import com.synkork.backend.modules.room.RoomRepository;
import com.synkork.backend.modules.room.enums.RoomStatusEnum;
import com.synkork.backend.modules.space.SpaceRepository;
import com.synkork.backend.modules.space.enums.SpaceStatusEnum;
import com.synkork.backend.modules.user.UserEntity;
import com.synkork.backend.modules.user.UserRepository;
import com.synkork.backend.modules.user.enums.PlanEnum;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class ExpiredSubscriptionScheduler {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private SpaceRepository spaceRepository;

    @Autowired
    private ExpiredSubscriptionService expiredSubscriptionService;

    @Autowired
    private ExpiredSubscriptionEmail expiredSubscriptionEmail;

    @Scheduled(cron = "0 10 00 * * *")
    public void remindUserToReNewSubscription() {
        LocalDateTime now = LocalDateTime.now();

        List<UserEntity> usersExpiringSoon = userRepository
                .findByPlanExpiresAtBetween(now, now.plusDays(3));

        List<List<UserEntity>> batches = Lists.partition(usersExpiringSoon, 50);
        for (List<UserEntity> batch : batches) {
            expiredSubscriptionService.pinPendingRemovalRoomAndSpace(batch);
        }
    }

    @Transactional
    @Scheduled(cron = "0 15 00 * * *")
    public void resetExpiredSubscriptions() {
        LocalDateTime now = LocalDateTime.now();

        List<String> userEmails = userRepository.findEmailByPlanExpiresAtBefore(now);
        for (String email : userEmails) {
            expiredSubscriptionEmail.sendPlanExpiredEmail(email);
        }

        roomRepository.lockExpiredOwnerRooms(RoomStatusEnum.PENDING_REMOVAL, RoomStatusEnum.LOCKED, now);
        spaceRepository.lockExpiredOwnerSpaces(SpaceStatusEnum.PENDING_REMOVAL, SpaceStatusEnum.LOCKED, now);

        userRepository.resetExpiredUsersToPlan(PlanEnum.FREE, now);
    }
}
