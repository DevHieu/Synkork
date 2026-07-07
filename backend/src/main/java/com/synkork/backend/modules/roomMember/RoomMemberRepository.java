package com.synkork.backend.modules.roomMember;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.synkork.backend.modules.roomMember.enums.RoomMemberRoleEnum;
import com.synkork.backend.modules.user.UserEntity;

@Repository
public interface RoomMemberRepository extends JpaRepository<RoomMemberEntity, UUID> {

    @Query("SELECT rm FROM RoomMemberEntity rm JOIN FETCH rm.user WHERE rm.user.id = :userId")
    Optional<RoomMemberEntity> findByUserId(@Param("userId") UUID userId);

    @Query("SELECT rm FROM RoomMemberEntity rm JOIN FETCH rm.user WHERE rm.user.id = :userId AND rm.room.id = :roomId")
    Optional<RoomMemberEntity> findByUserIdAndRoom_IdWithUser(
            @Param("userId") UUID userId,
            @Param("roomId") UUID roomId);

    List<RoomMemberEntity> findByRoom_Id(UUID roomId);

    Long countByRoom_Id(UUID id);

    boolean existsByRoom_IdAndUser_Id(UUID id, UUID userId);

    Optional<RoomMemberEntity> findByRoom_IdAndUser_Id(UUID roomId, UUID userId);

    Optional<RoomMemberEntity> findByRoom_IdAndId(UUID roomId, UUID memberId);

    @Query("SELECT rm.user FROM RoomMemberEntity rm WHERE rm.room.id = :roomUUID")
    List<UserEntity> findUsersByRoomId(UUID roomUUID);

    // Optional<RoomMemberEntity> findByUser_Email(String creatorEmail);

    @Query("SELECT rm FROM RoomMemberEntity rm JOIN FETCH rm.user WHERE rm.user.email = :email AND rm.room.id = :roomId")
    Optional<RoomMemberEntity> findByUser_EmailAndRoom_Id(
            // Query này fetch luôn user để tránh lỗi lazy khi dựng DTO tin nhắn.
            @Param("email") String email,
            @Param("roomId") UUID roomId);

    @Query("SELECT COUNT(rm) FROM RoomMemberEntity rm WHERE rm.user.id = :userId AND rm.role = :role AND rm.room.type = 'GROUP'")
    long countGroupRoomsByUserIdAndRole(@Param("userId") UUID userId, @Param("role") RoomMemberRoleEnum role);

    @Modifying
    @Query(value = "DELETE FROM card_assignees WHERE room_member_id = :roomMemberId", nativeQuery = true)
    void removeFromCardAssignees(@Param("roomMemberId") UUID roomMemberId);
}
