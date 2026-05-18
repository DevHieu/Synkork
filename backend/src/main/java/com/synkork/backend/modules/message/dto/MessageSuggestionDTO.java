package com.synkork.backend.modules.message.dto;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageSuggestionDTO {
    private UUID messageId;
    private boolean hasEvent;
    private String title;
    private String description;
    private String eventDate;
    private String startTime;
    private String endTime;

    public static MessageSuggestionDTO fromJsonNode(UUID messageId, JsonNode rootNode) {
        return new MessageSuggestionDTO(
                messageId,
                rootNode.path("hasEvent").asBoolean(false),
                readNullableText(rootNode, "title"),
                readNullableText(rootNode, "description"),
                readNullableText(rootNode, "eventDate"),
                readNullableText(rootNode, "startTime"),
                readNullableText(rootNode, "endTime")
        );
    }

    private static String readNullableText(JsonNode rootNode, String fieldName) {
        JsonNode fieldNode = rootNode.get(fieldName);
        if (fieldNode == null || fieldNode.isNull()) {
            return null;
        }

        String value = fieldNode.asText();
        return value == null || value.isBlank() ? null : value;
    }
}
