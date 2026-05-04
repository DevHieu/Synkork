package com.synkork.backend.modules.space;

import com.synkork.backend.modules.space.dto.CreateSpaceRequest;
import com.synkork.backend.modules.space.dto.SpaceDTO;
import com.synkork.backend.modules.space.dto.UpdateSpaceRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/rooms/{roomId}/spaces")
public class SpaceController {

    @Autowired
    private SpaceService spaceService;

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    @GetMapping
    public ResponseEntity<List<SpaceDTO>> getAllSpaceByRoomId(@PathVariable UUID roomId) {
        List<SpaceDTO> spaces = spaceService.getAllSpaceByRoomId(roomId);

        return ResponseEntity.ok(spaces);
    }

    @GetMapping("/{spaceId}")
    public ResponseEntity<SpaceDTO> getSpaceById(@PathVariable UUID spaceId) {
        return ResponseEntity.ok(spaceService.getSpaceById(spaceId));
    }

    @PostMapping
    public ResponseEntity<SpaceDTO> createSpace(@PathVariable String roomId, @RequestBody CreateSpaceRequest space) {
        UUID roomUUID = UUID.fromString(roomId);

        SpaceEntity entity = spaceService.createSpace(space, roomUUID);
        SpaceDTO dto = new SpaceDTO(entity);

        simpMessagingTemplate.convertAndSend("/topic/rooms/" + roomId + "/spaces/create", dto);

        return ResponseEntity.ok(dto);
    }

    @PutMapping("/{spaceId}")
    public ResponseEntity<SpaceDTO> updateSpace(@PathVariable String roomId, @PathVariable String spaceId, @RequestBody UpdateSpaceRequest space) {
        UUID spaceUUID = UUID.fromString(spaceId);

        SpaceEntity entity = spaceService.updateSpace(space, spaceUUID);
        SpaceDTO dto = new SpaceDTO(entity);

        simpMessagingTemplate.convertAndSend("/topic/rooms/" + roomId + "/spaces/update", dto);

        return  ResponseEntity.ok(dto);
    }


    @DeleteMapping("/{spaceId}")
    public ResponseEntity<SpaceDTO> deleteSpace(@PathVariable String roomId, @PathVariable String spaceId) {
        UUID spaceUUID = UUID.fromString(spaceId);

        spaceService.deleteSpace(spaceUUID);

        simpMessagingTemplate.convertAndSend("/topic/rooms/" + roomId + "/spaces/delete", spaceId);

        return  ResponseEntity.ok().build();
    }
}
