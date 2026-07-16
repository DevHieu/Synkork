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
import com.synkork.backend.modules.roomMember.enums.RoomMemberStatusEnum;
import com.synkork.backend.modules.user.UserEntity;

@Repository
public interface RoomMemberRepository extends JpaRepository<RoomMemberEntity, UUID> {

    @Query("SELECT rm FROM RoomMemberEntity rm JOIN FETCH rm.user WHERE rm.user.id = :userId AND rm.status = 'ACTIVE'")
    Optional<RoomMemberEntity> findByUserId(@Param("userId") UUID userId);

    @Query("SELECT rm FROM RoomMemberEntity rm JOIN FETCH rm.user WHERE rm.user.id = :userId AND rm.room.id = :roomId AND rm.status = 'ACTIVE'")
    Optional<RoomMemberEntity> findByUserIdAndRoom_IdWithUser(
            @Param("userId") UUID userId,
            @Param("roomId") UUID roomId);

    List<RoomMemberEntity> findByRoom_IdAndStatus(UUID roomId, RoomMemberStatusEnum status);

    default List<RoomMemberEntity> findByRoom_Id(UUID roomId) {
        return findByRoom_IdAndStatus(roomId, RoomMemberStatusEnum.ACTIVE);
    }

    Optional<RoomMemberEntity> findByIdAndRoom_IdAndStatus(UUID id, UUID roomId, RoomMemberStatusEnum status);

    default Optional<RoomMemberEntity> findByIdAndRoom_Id(UUID id, UUID roomId) {
        return findByIdAndRoom_IdAndStatus(id, roomId, RoomMemberStatusEnum.ACTIVE);
    }

    Long countByRoom_IdAndStatus(UUID id, RoomMemberStatusEnum status);

    default Long countByRoom_Id(UUID id) {
        return countByRoom_IdAndStatus(id, RoomMemberStatusEnum.ACTIVE);
    }

    boolean existsByRoom_IdAndUser_IdAndStatus(UUID id, UUID userId, RoomMemberStatusEnum status);

    default boolean existsByRoom_IdAndUser_Id(UUID id, UUID userId) {
        return existsByRoom_IdAndUser_IdAndStatus(id, userId, RoomMemberStatusEnum.ACTIVE);
    }

    Optional<RoomMemberEntity> findByRoom_IdAndUser_IdAndStatus(UUID roomId, UUID userId, RoomMemberStatusEnum status);

    default Optional<RoomMemberEntity> findByRoom_IdAndUser_Id(UUID roomId, UUID userId) {
        return findByRoom_IdAndUser_IdAndStatus(roomId, userId, RoomMemberStatusEnum.ACTIVE);
    }

    @Query("SELECT rm FROM RoomMemberEntity rm WHERE rm.room.id = :roomId AND rm.user.id = :userId")
    Optional<RoomMemberEntity> findIncludingInactiveByRoomIdAndUserId(
            @Param("roomId") UUID roomId,
            @Param("userId") UUID userId);

    Optional<RoomMemberEntity> findByIdAndUser_Id(UUID id, UUID userId);

    List<RoomMemberEntity> findByIdInAndRoom_IdAndStatus(
            List<UUID> ids,
            UUID roomId,
            RoomMemberStatusEnum status);

    @Query("SELECT rm FROM RoomMemberEntity rm JOIN FETCH rm.room WHERE rm.user.id = :userId AND rm.status = :status ORDER BY rm.joinedAt DESC")
    List<RoomMemberEntity> findByUserIdAndStatusWithRoom(
            @Param("userId") UUID userId,
            @Param("status") RoomMemberStatusEnum status);

    Optional<RoomMemberEntity> findByRoom_IdAndIdAndStatus(UUID roomId, UUID memberId, RoomMemberStatusEnum status);

    default Optional<RoomMemberEntity> findByRoom_IdAndId(UUID roomId, UUID memberId) {
        return findByRoom_IdAndIdAndStatus(roomId, memberId, RoomMemberStatusEnum.ACTIVE);
    }

    @Query("SELECT rm.user FROM RoomMemberEntity rm WHERE rm.room.id = :roomUUID AND rm.status = 'ACTIVE'")
    List<UserEntity> findUsersByRoomId(UUID roomUUID);

    // Optional<RoomMemberEntity> findByUser_Email(String creatorEmail);

    @Query("SELECT rm FROM RoomMemberEntity rm JOIN FETCH rm.user WHERE rm.user.email = :email AND rm.room.id = :roomId AND rm.status = 'ACTIVE'")
    Optional<RoomMemberEntity> findByUser_EmailAndRoom_Id(
            // Query này fetch luôn user để tránh lỗi lazy khi dựng DTO tin nhắn.
            @Param("email") String email,
            @Param("roomId") UUID roomId);

    @Query("SELECT COUNT(rm) FROM RoomMemberEntity rm WHERE rm.user.id = :userId AND rm.role = :role AND rm.status = 'ACTIVE' AND rm.room.type = 'GROUP'")
    long countGroupRoomsByUserIdAndRole(@Param("userId") UUID userId, @Param("role") RoomMemberRoleEnum role);

    @Modifying
    @Query(value = "DELETE FROM card_assignees WHERE room_member_id = :roomMemberId", nativeQuery = true)
    void removeFromCardAssignees(@Param("roomMemberId") UUID roomMemberId);

    @Modifying
    @Query(value = "DELETE FROM calendar_event_room_members WHERE room_member_id = :roomMemberId", nativeQuery = true)
    void removeFromCalendarEventRoomMembers(@Param("roomMemberId") UUID roomMemberId);
}
