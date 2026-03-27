package com.synkork.backend.modules.message;

import com.synkork.backend.modules.message.dto.MessageDTO;
import com.synkork.backend.modules.roomMember.RoomMemberEntity;
import com.synkork.backend.modules.roomMember.RoomMemberRepository;
import com.synkork.backend.modules.roomMember.dto.RoomMemberDto;
import com.synkork.backend.modules.space.SpaceEntity;
import com.synkork.backend.modules.space.SpaceRepository;
import com.synkork.backend.modules.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class MessageService {
    @Autowired
    MessageRepository messageRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    SpaceRepository spaceRepository;

    @Autowired
    private RoomMemberRepository roomMemberRepository;

    //   @NonNull annotation giúp đảm bảo rằng entity không được null, đỡ bị IDE báo
    public Optional<MessageEntity> createMessage(@NonNull MessageEntity entity) {
        return Optional.of(messageRepository.save(entity));
    }

    public MessageDTO saveMessage(MessageDTO dto, String senderId) {
        MessageEntity entity = new MessageEntity();
        UUID userId = UUID.fromString(senderId);
        UUID spaceId = UUID.fromString(dto.getSpaceId());

        SpaceEntity space = spaceRepository.findById(spaceId)
                .orElseThrow(() -> new IllegalArgumentException("Space not found"));

        RoomMemberEntity sender = roomMemberRepository.findByUserIdAndRoom_Id(userId, space.getRoom().getId())
                .orElseThrow(() -> new IllegalArgumentException("User is not a member of this room"));

        entity.setSender(sender);
        entity.setSpace(space);
        entity.setContent(dto.getContent());

        MessageEntity newMessage = messageRepository.save(entity);
        dto.setId(newMessage.getId());

        RoomMemberDto senderDto = new RoomMemberDto(sender);
        dto.setSender(senderDto);
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());

        return dto;
    }

    public Page<MessageDTO> getMessagesBySpaceId(UUID spaceId, Pageable pageable) {
        return messageRepository.findMessagesBySpaceId(spaceId, pageable);
    }

    public void deleteMessage(UUID messageId) {
        MessageEntity message = messageRepository.findById(messageId)
                .orElseThrow(() -> new IllegalArgumentException("Message not found"));

        message.setDeleted(true);
        messageRepository.save(message);
    }

    public MessageDTO updateMessage(MessageDTO dto) {
        MessageEntity  entity = messageRepository.findById(dto.getId()).orElseThrow(() -> new IllegalArgumentException("Message not found"));
        entity.setContent(dto.getContent());
        MessageEntity newMessage = messageRepository.save(entity);

        dto.setUpdatedAt(newMessage.getUpdatedAt());

        return dto;
    }
}
