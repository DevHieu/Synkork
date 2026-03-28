package com.synkork.backend.modules.friend;

import com.synkork.backend.modules.user.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FriendRepository extends JpaRepository<FriendEntity, UUID> {

    List<FriendEntity> findByUser(UserEntity user);

    Optional<FriendEntity> findByUserAndFriend(UserEntity user, UserEntity friend);

    void deleteByUserAndFriend(UserEntity user, UserEntity friend);
}