package com.synkork.backend.modules.space;

import com.synkork.backend.modules.space.dto.SpaceDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/rooms/{roomId}/spaces")
public class SpaceController {

    @Autowired
    private SpaceService spaceService;

    @PostMapping
    public ResponseEntity<SpaceDto> createSpace(@PathVariable UUID roomId, @RequestBody SpaceEntity space) {
        return spaceService.createSpace(space, roomId)
                .map(entity -> new SpaceDto(entity.getId(), entity.getName(), entity.getType()))
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.badRequest().body(null));
    }

    @GetMapping
    public ResponseEntity<List<SpaceDto>> getAllSpaceByRoomId(@PathVariable UUID roomId) {
        List<SpaceDto> spaces = spaceService.getAllSpaceByRoomId(roomId);

        return ResponseEntity.ok(spaces);
    }

    @DeleteMapping("/{spaceId}")
    public ResponseEntity<Void> deleteSpace(@PathVariable UUID roomId, @PathVariable UUID spaceId) {
        spaceService.deleteSpace(spaceId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{spaceId}")
    public ResponseEntity<SpaceDto> renameSpace(
            @PathVariable UUID roomId,
            @PathVariable UUID spaceId,
            @RequestBody Map<String, String> body) {
        String newName = body.get("name");
        if (newName == null || newName.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        SpaceDto renamed = spaceService.renameSpace(spaceId, newName.trim());
        return ResponseEntity.ok(renamed);
    }
}
