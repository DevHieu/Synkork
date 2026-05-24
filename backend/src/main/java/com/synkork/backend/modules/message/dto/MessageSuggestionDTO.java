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
    private String suggestionType;
    private boolean hasEvent;
    private boolean hasNote;
    private boolean hasTask;
    private String title;
    private String description;
    private String eventDate;
    private String startTime;
    private String endTime;
    private String noteTitle;
    private String noteContent;
    private String noteColor;
    private Boolean notePinned;
    private Boolean noteAllowEditAll;
    private String taskTitle;
    private String taskDescription;
    private String taskColumnName;
    private String taskDueDate;

    public static MessageSuggestionDTO fromJsonNode(UUID messageId, JsonNode rootNode) {
        // Gom payload suggestion về một DTO phẳng để frontend map theo messageId.
        String suggestionType = normalizeSuggestionType(readNullableText(rootNode, "suggestionType"));
        boolean hasEvent = readBoolean(rootNode, "hasEvent");
        boolean hasNote = readBoolean(rootNode, "hasNote");
        boolean hasTask = readBoolean(rootNode, "hasTask");

        if (suggestionType == null || suggestionType.isBlank()) {
            if (hasEvent) {
                suggestionType = "EVENT";
            } else if (hasNote) {
                suggestionType = "NOTE";
            } else if (hasTask) {
                suggestionType = "TASK";
            } else {
                suggestionType = "NONE";
            }
        }

        return new MessageSuggestionDTO(
                messageId,
                suggestionType,
                hasEvent,
                hasNote,
                hasTask,
                readNullableText(rootNode, "title"),
                readNullableText(rootNode, "description"),
                readNullableText(rootNode, "eventDate"),
                readNullableText(rootNode, "startTime"),
                readNullableText(rootNode, "endTime"),
                readNullableText(rootNode, "noteTitle"),
                readNullableText(rootNode, "noteContent"),
                readNullableText(rootNode, "noteColor"),
                readBooleanBoxed(rootNode, "notePinned"),
                readBooleanBoxed(rootNode, "noteAllowEditAll"),
                readNullableText(rootNode, "taskTitle"),
                readNullableText(rootNode, "taskDescription"),
                readNullableText(rootNode, "taskColumnName"),
                readNullableText(rootNode, "taskDueDate")
        );
    }

    public boolean isActionable() {
        // Chỉ những payload không phải NONE mới được đẩy sang websocket cho frontend xử lý.
        return suggestionType != null && !"NONE".equalsIgnoreCase(suggestionType);
    }

    private static String readNullableText(JsonNode rootNode, String fieldName) {
        JsonNode fieldNode = rootNode.get(fieldName);
        if (fieldNode == null || fieldNode.isNull()) {
            return null;
        }

        String value = fieldNode.asText();
        return value == null || value.isBlank() ? null : value;
    }

    private static boolean readBoolean(JsonNode rootNode, String fieldName) {
        JsonNode fieldNode = rootNode.get(fieldName);
        return fieldNode != null && !fieldNode.isNull() && fieldNode.asBoolean(false);
    }

    private static Boolean readBooleanBoxed(JsonNode rootNode, String fieldName) {
        JsonNode fieldNode = rootNode.get(fieldName);
        if (fieldNode == null || fieldNode.isNull()) {
            return null;
        }
        return fieldNode.asBoolean();
    }

    private static String normalizeSuggestionType(String suggestionType) {
        if (suggestionType == null || suggestionType.isBlank()) {
            return null;
        }
        return suggestionType.trim().toUpperCase();
    }
}
