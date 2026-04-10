package com.synkork.backend.modules.message;

import com.synkork.backend.modules.message.dto.MessageDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MessageRepository extends JpaRepository<MessageEntity, UUID> {

    @Query("""
            SELECT new com.synkork.backend.modules.message.dto.MessageDTO(
                m.id, m.content, m.space.id, m.deleted, m.pinned,
                m.type, m.attachmentUrl,
                rm.user.username, rm.user.displayName, rm.user.avatarUrl,
                rm.role, m.createdAt, m.updatedAt
                )
                FROM MessageEntity m
                JOIN m.sender rm
                WHERE m.space.id = :spaceId
                ORDER BY m.id DESC
                LIMIT :limit
            """)
    List<MessageDTO> findFirstPage(@Param("spaceId") UUID spaceId, @Param("limit") int limit);

    @Query("""
                SELECT new com.synkork.backend.modules.message.dto.MessageDTO(
                    m.id, m.content, m.space.id, m.deleted, m.pinned,
                    m.type, m.attachmentUrl,
                    rm.user.username, rm.user.displayName, rm.user.avatarUrl,
                    rm.role, m.createdAt, m.updatedAt
                    )
                FROM MessageEntity m
                JOIN m.sender rm
                WHERE m.space.id = :spaceId
                  AND m.id < :cursor
                ORDER BY m.id DESC
                LIMIT :limit
            """)
    List<MessageDTO> findNextPage(
            @Param("spaceId") UUID spaceId,
            @Param("cursor") UUID cursor,
            @Param("limit") int limit
    );

}
