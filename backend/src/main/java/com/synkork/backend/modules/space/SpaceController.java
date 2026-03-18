package com.synkork.backend.modules.space;

import com.synkork.backend.modules.space.dto.CreateSpaceDto;
import com.synkork.backend.modules.space.dto.SpaceDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/rooms/{roomId}/spaces")
public class SpaceController {

    @Autowired
    private SpaceService spaceService;

    @PostMapping
    public ResponseEntity<SpaceDto> createSpace(@PathVariable UUID roomId, @RequestBody CreateSpaceDto space) {
        System.out.println("space: " + space);
        return spaceService.createSpace(space, roomId)
                .map(s -> new SpaceDto(s.getId(), s.getName(), s.getType()))
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.badRequest().build());
    }

    @GetMapping
    public ResponseEntity<List<SpaceDto>> getAllSpaceByRoomId(@PathVariable UUID roomId) {
        List<SpaceDto> spaces = spaceService.getAllSpaceByRoomId(roomId);

        return ResponseEntity.ok(spaces);
    }
}
