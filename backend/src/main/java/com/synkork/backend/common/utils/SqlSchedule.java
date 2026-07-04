package com.synkork.backend.common.utils;

import com.google.common.collect.Lists;
import com.synkork.backend.modules.payment.ExpiredSubscriptionService;
import com.synkork.backend.modules.room.RoomEntity;
import com.synkork.backend.modules.room.RoomRepository;
import com.synkork.backend.modules.room.enums.RoomStatusEnum;
import com.synkork.backend.modules.admin.statistics.StatisticsService;
import com.synkork.backend.modules.space.SpaceRepository;
import com.synkork.backend.modules.space.enums.SpaceStatusEnum;
import com.synkork.backend.modules.user.UserEntity;
import com.synkork.backend.modules.user.UserRepository;
import com.synkork.backend.modules.user.enums.PlanEnum;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.core.Local;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class SqlSchedule {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private StatisticsService statisticsService;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SpaceRepository spaceRepository;
    @Autowired
    private ExpiredSubscriptionService expiredSubscriptionService;
    @Autowired
    private EmailService emailService;

    @Transactional
    @Scheduled(cron = "0 0 0 * * *")
    public void createStatistics() {
        statisticsService.createStatistics();
    }

    @Transactional
    // Giữa đêm sẽ chạy
    @Scheduled(cron = "0 0 0 * * *")
    public void deleteExpiredVerifications() {
        String sql = "DELETE FROM verification WHERE expired_at < NOW() - INTERVAL 15 MINUTES;"; // Xóa các verification quá hạn
        jdbcTemplate.execute(sql);
        System.out.println("delete verification sql successfully.");
    }

    // @Transactional
    // @Scheduled(cron = "0 0 0 * * *")
    // public void deleteRoomDeleted() {
    //     List<RoomEntity> rooms = roomRepository
    //             .findAllByStatusAndUpdatedAtBefore(
    //                     RoomStatusEnum.DELETED,
    //                     LocalDateTime.now().minusDays(30) // Delete 30 ngày thì mới xóa hẳn
    //             );

    //     roomRepository.deleteAll(rooms);

    //     System.out.println("Deleted rooms: " + rooms.size());
    // }

    @Scheduled(cron = "0 08 09 * * *")
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
    @Scheduled(cron = "0 04 09 * * *")
    public void resetExpiredSubscriptions() {
        List<String> userEmails = userRepository.findEmailByPlanExpiresAtAfter(LocalDateTime.now());
        for (String email : userEmails) {
            emailService.sendPlanExpiredEmail(email);
        }

        userRepository.resetExpiredUsersToPlan(PlanEnum.FREE, LocalDateTime.now());
        roomRepository.deleteByStatus(RoomStatusEnum.PENDING_REMOVAL);
        spaceRepository.deleteByStatus(SpaceStatusEnum.PENDING_REMOVAL);
    }
}
