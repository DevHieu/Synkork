package com.synkork.backend.modules.admin.workspace.rooms;

import com.synkork.backend.modules.room.RoomEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface AdminRoomRepository
        extends JpaRepository<RoomEntity, UUID>,
        JpaSpecificationExecutor<RoomEntity> {
}