package com.synkork.backend.modules.collaboration.note;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface NoteRepository extends JpaRepository<NoteEntity, UUID> {

    List<NoteEntity> findBySpaceId(UUID spaceUuid);
    List<NoteEntity> findByTitleContainingIgnoreCase(String title);
}
