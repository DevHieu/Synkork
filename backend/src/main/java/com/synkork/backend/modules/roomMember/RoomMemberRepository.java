package com.synkork.backend.modules.roomMember;

import com.synkork.backend.modules.user.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.synkork.backend.modules.collaboration.task.column.ColumnEntity;
import com.synkork.backend.modules.roomMember.enums.RoomMemberRoleEnum;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RoomMemberRepository extends JpaRepository<RoomMemberEntity, UUID> {

    @Query("SELECT rm FROM RoomMemberEntity rm JOIN FETCH rm.user WHERE rm.user.id = :userId")
    Optional<RoomMemberEntity> findByUserId(@Param("userId") UUID userId);

    @Query("SELECT rm FROM RoomMemberEntity rm JOIN FETCH rm.user WHERE rm.user.id = :userId AND rm.room.id = :roomId")
    Optional<RoomMemberEntity> findByUserIdAndRoom_IdWithUser(
            @Param("userId") UUID userId,
            @Param("roomId") UUID roomId
    );

    List<RoomMemberEntity> findByRoom_Id(UUID roomId);

    Long countByRoom_Id(UUID id);

    boolean existsByRoom_IdAndUser_Id(UUID id, UUID userId);

    Optional<RoomMemberEntity> findByRoom_IdAndUser_Id(UUID roomId, UUID userId);

    @Query("SELECT rm.user FROM RoomMemberEntity rm WHERE rm.room.id = :roomUUID")
    List<UserEntity> findUsersByRoomId(UUID roomUUID);

    // Optional<RoomMemberEntity> findByUser_Email(String creatorEmail);

    Optional<RoomMemberEntity> findByUser_EmailAndRoom_Id(String creatorEmail, UUID roomId);

    @Query("SELECT COUNT(rm) FROM RoomMemberEntity rm WHERE rm.user.id = :userId AND rm.role = :role AND rm.room.type = 'GROUP'")
long countGroupRoomsByUserIdAndRole(@Param("userId") UUID userId, @Param("role") RoomMemberRoleEnum role);
}
