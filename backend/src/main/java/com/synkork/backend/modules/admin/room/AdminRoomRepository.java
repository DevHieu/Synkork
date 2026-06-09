package com.synkork.backend.modules.admin.room;

import com.synkork.backend.modules.room.RoomEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface AdminRoomRepository
        extends JpaRepository<RoomEntity, UUID>,
                JpaSpecificationExecutor<RoomEntity> {
    @Query("""
        SELECT r FROM RoomEntity r
        LEFT JOIN FETCH r.owner
        LEFT JOIN FETCH r.roomMembers
        LEFT JOIN FETCH r.spaces
        WHERE r.id = :id
    """)
    Optional<RoomEntity> findDetailById(@Param("id") UUID id);
}