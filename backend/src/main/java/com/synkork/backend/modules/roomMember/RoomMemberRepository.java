package com.synkork.backend.modules.roomMember;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RoomMemberRepository extends JpaRepository<RoomMemberEntity, UUID> {

    @Query("SELECT rm FROM RoomMemberEntity rm JOIN FETCH rm.user WHERE rm.user.id = :userId")
    Optional<RoomMemberEntity> findByUserId(@Param("userId") UUID userId);

    Optional<RoomMemberEntity> findByUserIdAndRoom_Id(UUID userId, UUID roomId);

    List<RoomMemberEntity> findByRoom_Id(UUID roomId);


}
