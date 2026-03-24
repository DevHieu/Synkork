package com.synkork.backend.modules.message;

import com.synkork.backend.modules.message.dto.MessageDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface MessageRepository extends JpaRepository<MessageEntity, UUID> {

    @Query("""
                SELECT new com.synkork.backend.modules.message.dto.MessageDTO(
                    m.id,
                    m.content,
                    m.space.id,
                    m.deleted,
                    m.pinned,
                    m.type,
                    m.attachmentUrl,
                    rm.user.username,
                    rm.user.displayName,
                    rm.user.avatarUrl,
                    rm.role,
                    m.createdAt,
                    m.updatedAt
                )
                FROM MessageEntity m
                JOIN m.sender rm
                WHERE m.space.id = :spaceId
                ORDER BY m.createdAt DESC
            """)
    Page<MessageDTO> findMessagesBySpaceId(@Param("spaceId") UUID spaceId, Pageable pageable);
}
