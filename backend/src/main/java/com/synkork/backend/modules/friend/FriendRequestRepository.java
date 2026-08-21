package com.synkork.backend.modules.friend;

import com.synkork.backend.modules.friend.enums.FriendRequestStatus;
import com.synkork.backend.modules.user.UserEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FriendRequestRepository extends JpaRepository<FriendRequestEntity, UUID> {

    Optional<FriendRequestEntity> findBySenderAndReceiver(UserEntity sender, UserEntity receiver);

    @EntityGraph(attributePaths = {"sender", "receiver"})
    List<FriendRequestEntity> findByReceiverAndStatus(UserEntity receiver, FriendRequestStatus status);

    @EntityGraph(attributePaths = {"sender", "receiver"})
    List<FriendRequestEntity> findBySenderAndStatus(UserEntity sender, FriendRequestStatus status);
}