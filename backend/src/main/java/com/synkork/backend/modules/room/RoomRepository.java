package com.synkork.backend.modules.room;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RoomRepository extends JpaRepository<RoomEntity, UUID> {
    List<RoomEntity> findAllByOwnerId(UUID userId);

    @Query("SELECT r FROM RoomEntity r JOIN r.roomMembers roomMembers WHERE roomMembers.user.id = :userId ORDER BY roomMembers.joinedAt DESC")
    List<RoomEntity> findRoomMembersJoined(@Param("userId") UUID userId);
}
