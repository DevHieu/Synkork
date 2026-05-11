package com.synkork.backend.common.utils;

import com.synkork.backend.modules.room.RoomEntity;
import com.synkork.backend.modules.room.RoomRepository;
import com.synkork.backend.modules.room.enums.RoomStatusEnum;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
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
    private RoomRepository roomRepository;

    @Transactional
    // Giữa đêm sẽ chạy
    @Scheduled(cron = "0 0 0 * * *")
    public void deleteExpiredVerifications() {
        String sql = "DELETE FROM verification WHERE expired_at < NOW() - INTERVAL 15 MINUTES;"; // Xóa các verification quá hạn
        jdbcTemplate.execute(sql);
        System.out.println("delete verification sql successfully.");
    }

    @Transactional
    @Scheduled(cron = "0 0 0 * * *")
    public void deleteRoomDeleted() {
        List<RoomEntity> rooms = roomRepository
                .findAllByStatusAndUpdatedAtBefore(
                        RoomStatusEnum.DELETED,
                        LocalDateTime.now().minusDays(30) // Delete 30 ngày thì mới xóa hẳn
                );

        roomRepository.deleteAll(rooms);

        System.out.println("Deleted rooms: " + rooms.size());
    }
}
