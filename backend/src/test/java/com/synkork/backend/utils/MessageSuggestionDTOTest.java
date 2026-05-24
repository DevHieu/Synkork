package com.synkork.backend.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.synkork.backend.modules.message.dto.MessageSuggestionDTO;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class MessageSuggestionDTOTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void shouldParseEventSuggestion() throws Exception {
        String json = """
                {
                  "suggestionType": "EVENT",
                  "hasEvent": true,
                  "title": "Họp team",
                  "description": "Họp lúc 9h",
                  "eventDate": "2026-05-21",
                  "startTime": "09:00",
                  "endTime": "10:00"
                }
                """;

        MessageSuggestionDTO dto = MessageSuggestionDTO.fromJsonNode(UUID.randomUUID(), objectMapper.readTree(json));

        assertEquals("EVENT", dto.getSuggestionType());
        assertTrue(dto.isHasEvent());
        assertTrue(dto.isActionable());
        assertEquals("Họp team", dto.getTitle());
        assertEquals("09:00", dto.getStartTime());
    }

    @Test
    void shouldParseNoteSuggestion() throws Exception {
        String json = """
                {
                  "suggestionType": "NOTE",
                  "hasNote": true,
                  "noteTitle": "Nhớ mua nước",
                  "noteContent": "Mua nước cho buổi họp",
                  "notePinned": true,
                  "noteColor": "#f97316"
                }
                """;

        MessageSuggestionDTO dto = MessageSuggestionDTO.fromJsonNode(UUID.randomUUID(), objectMapper.readTree(json));

        assertEquals("NOTE", dto.getSuggestionType());
        assertTrue(dto.isHasNote());
        assertTrue(dto.isActionable());
        assertEquals("Nhớ mua nước", dto.getNoteTitle());
        assertEquals("Mua nước cho buổi họp", dto.getNoteContent());
        assertTrue(Boolean.TRUE.equals(dto.getNotePinned()));
    }

    @Test
    void shouldFallbackToNoneWhenTypeIsMissing() throws Exception {
        String json = """
                {
                  "title": "Xin chào"
                }
                """;

        MessageSuggestionDTO dto = MessageSuggestionDTO.fromJsonNode(UUID.randomUUID(), objectMapper.readTree(json));

        assertEquals("NONE", dto.getSuggestionType());
        assertFalse(dto.isActionable());
    }
}
