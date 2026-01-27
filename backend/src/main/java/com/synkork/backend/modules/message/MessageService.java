package com.synkork.backend.modules.message;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.synkork.backend.modules.message.dto.MessageDTO;
import com.synkork.backend.modules.message.dto.MessageProjection;
import com.synkork.backend.modules.space.SpaceRepository;
import com.synkork.backend.modules.user.UserEntity;
import com.synkork.backend.modules.user.UserRepository;
import com.synkork.backend.modules.user.dto.SenderDto;
import com.synkork.backend.modules.user.enums.RoleEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;

@Service
public class MessageService {
  @Autowired
  MessageRepository messageRepository;

  @Autowired
  UserRepository userRepository;

    @Autowired
    SpaceRepository spaceRepository;

//   @NonNull annotation giúp đảm bảo rằng entity không được null, đỡ bị IDE báo
  public Optional<MessageEntity> createMessage(@NonNull MessageEntity entity) {
    return Optional.ofNullable(messageRepository.save(entity));
  }

    public MessageDTO saveMessage(MessageDTO dto, String senderId) {
      MessageEntity entity = new MessageEntity();
        System.out.println("userId = [" + senderId + "]");
        UUID userId = UUID.fromString(senderId);

      UserEntity sender = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found"));

      entity.setSender(sender);
      entity.setSpace(spaceRepository.getReferenceById(UUID.fromString(dto.getSpaceId())));
      entity.setContent(dto.getContent());

        messageRepository.save(entity);

        SenderDto senderDto = new SenderDto(sender.getUsername(), sender.getDisplayName(), sender.getAvatarUrl(), RoleEnum.valueOf(sender.getRole()));

        dto.setSender(senderDto);
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());

        return dto;
    }

    public Page<MessageDTO> getMessagesBySpaceId(UUID spaceId, Pageable pageable) {
      Page<MessageProjection> messageProjections = messageRepository.findBySpace_Id(spaceId, pageable);
        System.out.println(messageProjections.getTotalElements());
      return messageProjections.map(MessageDTO::new);
    }
}
