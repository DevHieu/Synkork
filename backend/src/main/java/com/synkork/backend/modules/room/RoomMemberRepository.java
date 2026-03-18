package com.synkork.backend.modules.room;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface RoomMemberRepository extends JpaRepository<RoomMemberEntity, UUID> {
}
