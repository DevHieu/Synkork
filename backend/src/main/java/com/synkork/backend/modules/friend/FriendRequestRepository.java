package com.synkork.backend.modules.friend;

import com.synkork.backend.modules.friend.enums.FriendRequestStatus;
import com.synkork.backend.modules.user.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FriendRequestRepository extends JpaRepository<FriendRequestEntity, Long> {

    Optional<FriendRequestEntity> findBySenderAndReceiver(UserEntity sender, UserEntity receiver);

    List<FriendRequestEntity> findByReceiverAndStatus(UserEntity receiver, FriendRequestStatus status);
}