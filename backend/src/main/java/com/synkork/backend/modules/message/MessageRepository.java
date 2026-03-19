package com.synkork.backend.modules.message;

import com.synkork.backend.modules.message.dto.MessageProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MessageRepository extends JpaRepository<MessageEntity, UUID> {

    Page<MessageProjection> findBySpace_Id(UUID spaceId, Pageable pageable);
}
