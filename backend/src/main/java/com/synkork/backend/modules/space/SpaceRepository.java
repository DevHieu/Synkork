package com.synkork.backend.modules.space;

import com.synkork.backend.modules.room.RoomEntity;
import com.synkork.backend.modules.space.dto.SpaceDTO;
import com.synkork.backend.modules.space.enums.SpaceStatusEnum;
import com.synkork.backend.modules.space.enums.SpaceTypeEnum;

import org.springframework.data.domain.Pageable;
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
            "FROM SpaceEntity s WHERE s.room.id = :roomId ORDER BY s.createdAt ASC")
    List<SpaceDTO> findAllByRoomIdAsDto(@Param("roomId") UUID roomId);

    long countByRoom_IdAndType(UUID roomId, SpaceTypeEnum type);

    void deleteByStatus(SpaceStatusEnum spaceStatusEnum);

    List<SpaceEntity> findByRoomIdAndTypeOrderByCreatedAtDesc(UUID roomId, SpaceTypeEnum type);

    @Modifying
    @Query("UPDATE SpaceEntity s SET s.status = :status WHERE s.id IN :ids")
    void updateStatusByIds(@Param("status") SpaceStatusEnum status, @Param("ids") List<UUID> ids);
}
