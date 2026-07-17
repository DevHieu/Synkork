package com.synkork.backend.modules.space;

import com.synkork.backend.modules.room.enums.RoomStatusEnum;
import com.synkork.backend.modules.space.dto.SpaceDTO;
import com.synkork.backend.modules.space.enums.SpaceStatusEnum;
import com.synkork.backend.modules.space.enums.SpaceTypeEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SpaceRepository extends JpaRepository<SpaceEntity, UUID> {

    @Query("SELECT new com.synkork.backend.modules.space.dto.SpaceDTO(s.id, s.name, s.type, s.room.type, s.isRestricted) " +
            "FROM SpaceEntity s WHERE s.room.id = :roomId AND s.status = 'OPEN' ORDER BY s.createdAt ASC")
    List<SpaceDTO> findAllByRoomIdAsDto(@Param("roomId") UUID roomId);

    long countByRoom_Id(UUID roomId);

    long countByRoom_IdAndType(UUID roomId, SpaceTypeEnum type);

    void deleteByStatus(SpaceStatusEnum spaceStatusEnum);

    List<SpaceEntity> findByRoomIdOrderByCreatedAtDesc(UUID roomId);
    List<SpaceEntity> findByRoomIdAndTypeAndStatusInOrderByCreatedAtDesc(UUID roomId, SpaceTypeEnum type, List<SpaceStatusEnum> statuses);

    @Modifying
    @Query("UPDATE SpaceEntity s SET s.status = :status WHERE s.id IN :ids")
    void updateStatusByIds(@Param("status") SpaceStatusEnum status, @Param("ids") List<UUID> ids);

    @Modifying
    @Query("UPDATE SpaceEntity s SET s.status = :newStatus WHERE s.room.owner.id = :ownerId AND s.status = 'PENDING_REMOVAL'")
    void updatePendingSpaceStatusByRoom_OwnerId(@Param("newStatus") SpaceStatusEnum status, @Param("ownerId") UUID ownerId);

    @Modifying
    @Query("UPDATE SpaceEntity s SET s.status = :newStatus WHERE s.status = :oldStatus")
    void updateStatusByStatus(@Param("oldStatus") SpaceStatusEnum oldStatus, @Param("newStatus") SpaceStatusEnum newStatus);

    @Query("""
             SELECT (COUNT(s) > 0)
             FROM SpaceEntity s
             LEFT JOIN RoomMemberEntity rm ON s.room.id = rm.room.id
             WHERE s.id = :spaceId
             AND (
                 rm.user.id = :userId
                OR s.room.owner.id = :userId
            )
            """)
    boolean hasAccess(@Param("spaceId") UUID spaceId, @Param("userId") UUID userId);
}
