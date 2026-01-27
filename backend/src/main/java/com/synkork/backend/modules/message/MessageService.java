package com.synkork.backend.modules.message;

import java.util.Optional;
import java.util.UUID;

import com.synkork.backend.modules.message.dto.MessageDTO;
import com.synkork.backend.modules.space.SpaceRepository;
import com.synkork.backend.modules.user.UserEntity;
import com.synkork.backend.modules.user.UserRepository;
import com.synkork.backend.modules.user.dto.SenderDto;
import com.synkork.backend.modules.user.enums.RoleEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

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
}
