package com.synkork.backend.modules.roomMember;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.synkork.backend.modules.room.enums.RoomTypeEnum;
import com.synkork.backend.modules.roomMember.enums.MemberStatusEnum;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.synkork.backend.modules.roomMember.enums.RoomMemberRoleEnum;
import com.synkork.backend.modules.user.UserEntity;

@Repository
public interface RoomMemberRepository extends JpaRepository<RoomMemberEntity, UUID> {

    List<RoomMemberEntity> findByUserId(UUID userId);

    @Query("SELECT rm FROM RoomMemberEntity rm JOIN FETCH rm.room r LEFT JOIN FETCH r.owner WHERE rm.user.id = :userId")
    List<RoomMemberEntity> findByUserIdWithRoom(@Param("userId") UUID userId);

    @Query("SELECT rm FROM RoomMemberEntity rm JOIN FETCH rm.user WHERE rm.user.id = :userId AND rm.room.id = :roomId")
    Optional<RoomMemberEntity> findByUserIdAndRoom_IdWithUser(
            @Param("userId") UUID userId,
            @Param("roomId") UUID roomId);

    List<RoomMemberEntity> findByRoom_Id(UUID roomId);

    Optional<RoomMemberEntity> findByIdAndRoom_Id(UUID id, UUID roomId);

    List<RoomMemberEntity> findByRoom_IdAndStatus(UUID roomId, MemberStatusEnum status);

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

    @Query("SELECT COUNT(rm) FROM RoomMemberEntity rm WHERE rm.user.id = :userId AND rm.role = :role AND rm.room.type = 'GROUP' AND rm.status = 'ACTIVE'")
    long countGroupRoomsByUserIdAndRole(@Param("userId") UUID userId, @Param("role") RoomMemberRoleEnum role);

    @Query("""
            SELECT COUNT(rm)
            FROM RoomMemberEntity rm
            WHERE rm.room.type = :roomType
              AND (:start IS NULL OR rm.room.createdAt >= :start)
              AND (:end IS NULL OR rm.room.createdAt <= :end)
            """)
    long countByRoomTypeAndRoomCreatedAtBetween(
            @Param("roomType") RoomTypeEnum roomType,
            @Param("start") java.time.LocalDateTime start,
            @Param("end") java.time.LocalDateTime end
    );

//    @Modifying
//    @Query(value = "DELETE FROM card_assignees WHERE room_member_id = :roomMemberId", nativeQuery = true)
//    void removeFromCardAssignees(@Param("roomMemberId") UUID roomMemberId);
//
//    @Modifying
//    @Query(value = "DELETE FROM calendar_event_room_members WHERE room_member_id = :roomMemberId", nativeQuery = true)
//    void removeFromCalendarEventRoomMembers(@Param("roomMemberId") UUID roomMemberId);

    @Modifying
    @Transactional
    @Query("""
        UPDATE RoomMemberEntity rm
        SET rm.status = :status
        WHERE rm.user.id = :userId
    """)
    int updateStatusByUserId(
            @Param("userId") UUID userId,
            @Param("status") MemberStatusEnum status
    );

    @Modifying
    @Transactional
    @Query("""
        UPDATE RoomMemberEntity rm
        SET rm.status = 'INACTIVE'
        WHERE rm.user.id = :userId
          AND rm.status = 'ACTIVE'
    """)
    int deactivateActiveMembersByAdminLock(@Param("userId") UUID userId);

    @Modifying
    @Transactional
    @Query("""
        UPDATE RoomMemberEntity rm
        SET rm.status = 'ACTIVE'
        WHERE rm.user.id = :userId
          AND rm.status = 'INACTIVE'
    """)
    int restoreMembersInactiveByAdminLock(@Param("userId") UUID userId);

    @Modifying
    @Transactional
    @Query("""
        UPDATE RoomMemberEntity rm
        SET rm.role = :role
        WHERE rm.user.id = :userId
    """)
    int updateRoleByUserId(
            @Param("userId") UUID userId,
            @Param("role") RoomMemberRoleEnum role
    );

    @Modifying
    @Transactional
    @Query("""
        UPDATE RoomMemberEntity rm
        SET rm.role = :role
        WHERE rm.user.id = :userId
          AND rm.status = 'INACTIVE'
    """)
    int updateRoleByUserIdAndInactiveStatus(
            @Param("userId") UUID userId,
            @Param("role") RoomMemberRoleEnum role
    );
}
