package com.synkork.backend.modules.room;

import com.synkork.backend.modules.room.enums.RoomStatusEnum;
import com.synkork.backend.modules.room.enums.RoomTypeEnum;
import com.synkork.backend.modules.user.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
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

    @Query("SELECT r FROM RoomEntity r JOIN r.roomMembers roomMembers WHERE roomMembers.user.id = :userId AND r.type = 'GROUP' AND r.status IN ('OPEN', 'PENDING_REMOVAL') AND roomMembers.status = 'ACTIVE' ORDER BY roomMembers.joinedAt DESC")
    List<RoomEntity> findRoomMembersJoined(@Param("userId") UUID userId);

    @Query("SELECT r.owner FROM RoomEntity r WHERE r.id = :roomId")
    Optional<UserEntity> findOwnerByRoomId(@Param("roomId") UUID roomId);

    Optional<RoomEntity> findByInviteCode(String inviteCode);

    List<RoomEntity> findAllByStatusAndUpdatedAtBefore(RoomStatusEnum status, LocalDateTime updatedAtBefore);
 
    long countByType(RoomTypeEnum type);

    long countByTypeAndCreatedAtBetween(RoomTypeEnum type, LocalDateTime from, LocalDateTime to);

    long countByCreatedAtBetweenAndType(LocalDateTime createdAtAfter, LocalDateTime createdAtBefore, RoomTypeEnum type);

    void deleteByStatus(RoomStatusEnum roomStatusEnum);

    List<RoomEntity> findByOwnerIdAndTypeAndStatusInOrderByCreatedAtDesc(
            UUID ownerId,
            RoomTypeEnum type,
            List<RoomStatusEnum> statuses
    );

    @Modifying
    @Query("UPDATE RoomEntity r SET r.status = :status WHERE r.id IN :ids")
    void updateStatusByIds(@Param("status") RoomStatusEnum status, @Param("ids") List<UUID> ids);

    @Modifying
    @Query("UPDATE RoomEntity r SET r.status = :newStatus WHERE r.owner.id = :ownerId AND r.status = 'PENDING_REMOVAL'")
    void updatePendingRoomStatusByOwnerId(@Param("newStatus") RoomStatusEnum status, @Param("ownerId") UUID ownerId);

    @Modifying
    @Query("UPDATE RoomEntity r SET r.status = :newStatus WHERE r.status = :oldStatus")
    void updateStatusByStatus(@Param("oldStatus") RoomStatusEnum oldStatus, @Param("newStatus") RoomStatusEnum newStatus);
}
