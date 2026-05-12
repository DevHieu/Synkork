package com.synkork.backend.modules.room;

import com.synkork.backend.modules.room.enums.RoomStatusEnum;
import com.synkork.backend.modules.room.enums.RoomTypeEnum;
import com.synkork.backend.modules.statistics.dtos.CountByDate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RoomRepository extends JpaRepository<RoomEntity, UUID> {
    List<RoomEntity> findAllByOwnerId(UUID userId);

    @Query("SELECT r FROM RoomEntity r JOIN r.roomMembers roomMembers WHERE roomMembers.user.id = :userId AND r.type = 'GROUP' AND r.status = 'OPEN' ORDER BY roomMembers.joinedAt DESC")
    List<RoomEntity> findRoomMembersJoined(@Param("userId") UUID userId);

    Optional<RoomEntity> findByInviteCode(String inviteCode);

    List<RoomEntity> findAllByStatusAndUpdatedAtBefore(RoomStatusEnum status, LocalDateTime updatedAtBefore);

    long countByType(RoomTypeEnum type);

    long countByCreatedAtBetweenAndType(LocalDateTime createdAtAfter, LocalDateTime createdAtBefore, RoomTypeEnum type);
}
