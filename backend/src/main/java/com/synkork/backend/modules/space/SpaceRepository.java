package com.synkork.backend.modules.space;

import com.synkork.backend.modules.space.dto.SpaceDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SpaceRepository extends JpaRepository<SpaceEntity, UUID> {

    @Query("SELECT new com.synkork.backend.modules.space.dto.SpaceDto(s.id, s.name, s.type) " +
            "FROM SpaceEntity s WHERE s.room.id = :roomId")
    List<SpaceDto> findAllByRoomIdAsDto(@Param("roomId") UUID roomId);
}
